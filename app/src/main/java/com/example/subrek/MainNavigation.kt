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
    navController: NavHostController = rememberNavController()
) {
    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val onboardingUiState by onboardingViewModel.uiState.collectAsState()

    // Jika data onboarding completed masih memuat (null), berikan layar pemuat sementara yang aman
    if (onboardingUiState.isOnboardingCompleted == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Tentukan layar pertama berdasarkan status onboarding
    val startDestination = remember(onboardingUiState.isOnboardingCompleted) {
        if (onboardingUiState.isOnboardingCompleted == true) {
            Screen.Dashboard.route
        } else {
            Screen.Onboarding.route
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
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Dashboard.route) {
                val viewModel: DashboardViewModel = hiltViewModel()
                val profileViewModel: ProfileViewModel = hiltViewModel()
                DashboardScreen(
                    viewModel = viewModel,
                    profileViewModel = profileViewModel,
                    onNavigateToAddSubscription = {
                        navController.navigate(Screen.TambahLangganan.route)
                    },
                    onNavigateToDetail = { subscriptionId ->
                        navController.navigate(Screen.SubscriptionDetail.createRoute(subscriptionId))
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
                    viewModel = viewModel
                    // Catatan: Pastikan Anda juga menghapus argumen 'onNavigateToLogin'
                    // di dalam komponen ProfileScreen.kt Anda, karena fitur logout sudah tidak ada.
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