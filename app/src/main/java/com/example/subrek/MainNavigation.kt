package com.example.subrek

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.subrek.features.auth.presentation.AuthViewModel
import com.example.subrek.features.auth.presentation.screens.AuthScreen
import com.example.subrek.features.dashboard.presentation.screens.DashboardScreen
import com.example.subrek.features.dashboard.presentation.viewmodel.DashboardViewModel
import com.example.subrek.features.onboarding.presentation.screens.OnboardingScreen
import com.example.subrek.features.onboarding.presentation.viewmodel.OnboardingViewModel
import com.example.subrek.features.profile.presentation.screens.ProfileScreen
import com.example.subrek.features.profile.presentation.viewmodel.ProfileViewModel
import com.example.subrek.features.report.presentation.screens.ReportScreen
import com.example.subrek.features.report.presentation.viewmodel.ReportViewModel
import com.example.subrek.features.subscription.presentation.screens.AddSubscriptionScreen
import com.example.subrek.features.subscription.presentation.screens.SubscriptionDetailScreen
import com.example.subrek.features.subscription.presentation.screens.TambahLanggananScreen
import com.example.subrek.features.subscription.presentation.viewmodel.AddSubscriptionViewModel
import com.example.subrek.features.subscription.presentation.viewmodel.SubscriptionDetailViewModel
import com.example.subrek.features.subscription.presentation.viewmodel.TambahLanggananViewModel

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Auth : Screen("auth_screen")
    object Dashboard : Screen("homepage")
    object TambahLangganan : Screen("tambah_langganan")
    object Profile : Screen("profile")
    object Report : Screen("report")
    object AddSubscription : Screen("add_subscription")
    object SubscriptionDetail : Screen("subscription_detail/{subscriptionId}") {
        fun createRoute(id: String) = "subscription_detail/$id"
    }
}

// Route yang menampilkan BottomNavbar
private val bottomNavRoutes = setOf(
    Screen.Dashboard.route,
    Screen.TambahLangganan.route,
    Screen.Profile.route
)

@Composable
fun MainNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val isUserLoggedIn by authViewModel.isUserLoggedIn.collectAsState()
    val onboardingUiState by onboardingViewModel.uiState.collectAsState()

    if (isUserLoggedIn == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination = remember(isUserLoggedIn, onboardingUiState.isOnboardingCompleted) {
        when {
            isUserLoggedIn == true -> Screen.Dashboard.route
            !onboardingUiState.isOnboardingCompleted -> Screen.Onboarding.route
            else -> Screen.Auth.route
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                SubrekBottomNavbar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
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
                val viewModel: DashboardViewModel = hiltViewModel()
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToAddSubscription = {
                        navController.navigate(Screen.TambahLangganan.route)
                    }
                )
            }

            composable(Screen.TambahLangganan.route) {
                val viewModel: TambahLanggananViewModel = hiltViewModel()
                TambahLanggananScreen(
                    viewModel = viewModel,
                    onSuccessSave = { navController.popBackStack() }
                )
            }

            composable(Screen.Profile.route) {
                val viewModel: ProfileViewModel = hiltViewModel()
                ProfileScreen(
                    viewModel = viewModel,
                    onNavigateToLogin = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Report.route) {
                val viewModel: ReportViewModel = hiltViewModel()
                ReportScreen(viewModel = viewModel)
            }

            composable(Screen.AddSubscription.route) {
                val viewModel: AddSubscriptionViewModel = hiltViewModel()
                AddSubscriptionScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.SubscriptionDetail.route,
                arguments = listOf(navArgument("subscriptionId") { type = NavType.StringType })
            ) {
                val viewModel: SubscriptionDetailViewModel = hiltViewModel()
                SubscriptionDetailScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
