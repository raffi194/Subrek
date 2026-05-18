package com.example.subrek

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.subrek.features.onboarding.presentation.screens.OnboardingScreen
import com.example.subrek.features.onboarding.presentation.viewmodel.OnboardingViewModel
import com.example.subrek.features.subscription.presentation.screens.AddSubscriptionScreen
import com.example.subrek.features.subscription.presentation.screens.SubscriptionListScreen
import com.example.subrek.features.subscription.presentation.viewmodel.AddSubscriptionViewModel
import com.example.subrek.features.subscription.presentation.viewmodel.SubscriptionViewModel

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object AddSubscription : Screen("add_subscription")
}

@Composable
fun MainNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            val viewModel: OnboardingViewModel = hiltViewModel()
            OnboardingScreen(
                viewModel = viewModel,
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
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
