package com.example.subrek.features.onboarding.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.subrek.features.onboarding.domain.usecase.CheckFirstLaunchUseCase
import com.example.subrek.features.onboarding.domain.usecase.CompleteOnboardingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class OnboardingUiState(
    val isFirstLaunch: Boolean = true,
    val currentPage: Int = 0,
    val isOnboardingCompleted: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val checkFirstLaunchUseCase: CheckFirstLaunchUseCase,
    private val completeOnboardingUseCase: CompleteOnboardingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        val firstLaunch = checkFirstLaunchUseCase()
        _uiState.value = _uiState.value.copy(
            isFirstLaunch = firstLaunch,
            isOnboardingCompleted = !firstLaunch // Jika bukan yang pertama, tandai langsung selesai
        )
    }

    fun setCurrentPage(page: Int) {
        _uiState.value = _uiState.value.copy(currentPage = page)
    }

    fun completeOnboarding() {
        completeOnboardingUseCase()
        _uiState.value = _uiState.value.copy(isOnboardingCompleted = true)
    }
}
