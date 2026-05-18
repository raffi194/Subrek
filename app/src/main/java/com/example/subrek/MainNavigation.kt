package com.example.subrek

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.subrek.features.auth.presentation.AuthViewModel
import com.example.subrek.features.auth.presentation.screens.AuthScreen
import com.example.subrek.features.onboarding.presentation.screens.OnboardingScreen
import com.example.subrek.features.onboarding.presentation.viewmodel.OnboardingViewModel
import com.example.subrek.features.subscription.presentation.screens.AddSubscriptionScreen
import com.example.subrek.features.subscription.presentation.screens.SubscriptionListScreen
import com.example.subrek.features.subscription.presentation.viewmodel.AddSubscriptionViewModel
import com.example.subrek.features.subscription.presentation.viewmodel.SubscriptionViewModel

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Auth : Screen("auth_screen")
    object Dashboard : Screen("homepage")
    object AddSubscription : Screen("add_subscription")
}

@Composable
fun MainNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val isUserLoggedIn by authViewModel.isUserLoggedIn.collectAsState()
    val onboardingUiState by onboardingViewModel.uiState.collectAsState()

    // Menunggu pengecekan sesi selesai
    if (isUserLoggedIn == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Logika penentuan Start Destination berdasarkan instruksi kriteria masuk
    val startDestination = remember(isUserLoggedIn, onboardingUiState.isOnboardingCompleted) {
        when {
            isUserLoggedIn == true -> Screen.Dashboard.route                             // User Lama (Bypass langsung)
            !onboardingUiState.isOnboardingCompleted -> Screen.Onboarding.route        // User Baru -> Harus onboarding dulu
            else -> Screen.Auth.route                                                   // Masuk ke form login/register
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                viewModel = onboardingViewModel,
                onNavigateToDashboard = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Auth.route) {
            AuthScreen(
                viewModel = authViewModel,
                onAuthSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            val viewModel: SubscriptionViewModel = hiltViewModel()
            SubscriptionListScreen(
                viewModel = viewModel,
                onNavigateToAdd = {
                    navController.navigate(Screen.AddSubscription.route)
                }
            )
        }

        composable(Screen.AddSubscription.route) {
            val viewModel: AddSubscriptionViewModel = hiltViewModel()
            AddSubscriptionScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
