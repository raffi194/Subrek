package com.example.subrek

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.subrek.core.theme.SubrekTheme
import com.example.subrek.features.auth.domain.usecase.CheckAuthSessionUseCase
import com.example.subrek.features.onboarding.presentation.viewmodel.OnboardingViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var checkAuthSessionUseCase: CheckAuthSessionUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SubrekTheme {
                val onboardingViewModel: OnboardingViewModel = hiltViewModel()
                val onboardingState by onboardingViewModel.uiState.collectAsState()
                
                // Observe current session
                val currentUserId by checkAuthSessionUseCase().collectAsState(initial = null)
                
                // Logika Alur Masuk (Sesi) sesuai Step 4.3:
                // 1. User Lama (Sudah Login): Bypass ke Homepage
                // 2. User Baru: Onboarding -> Register/Login
                val startDestination = remember(onboardingState.isOnboardingCompleted, currentUserId) {
                    when {
                        currentUserId != null -> Screen.Dashboard.route
                        onboardingState.isOnboardingCompleted -> Screen.Auth.route
                        else -> Screen.Onboarding.route
                    }
                }

// Tampilkan splash/loading jika data belum siap (opsional, tapi di sini langsung render)
                MainNavigation()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}
