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
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.subrek.core.theme.SubrekTheme
import com.example.subrek.features.onboarding.presentation.viewmodel.OnboardingViewModel

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SubrekTheme {
                val onboardingViewModel: OnboardingViewModel = hiltViewModel()
                val onboardingState by onboardingViewModel.uiState.collectAsState()
                
                // Cek status pertama kali untuk menentukan startDestination
                val startDestination = if (onboardingState.isOnboardingCompleted) {
                    Screen.Dashboard.route
                } else {
                    Screen.Onboarding.route
                }

                MainNavigation(startDestination = startDestination)
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
