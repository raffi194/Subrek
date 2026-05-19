package com.example.subrek.features.onboarding.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subrek.features.onboarding.domain.usecase.CheckFirstLaunchUseCase
import com.example.subrek.features.onboarding.domain.usecase.CompleteOnboardingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val isFirstLaunch: Boolean = true,
    val currentPage: Int = 0,
    val isOnboardingCompleted: Boolean? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val checkFirstLaunchUseCase: CheckFirstLaunchUseCase,
    private val completeOnboardingUseCase: CompleteOnboardingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val firstLaunch = checkFirstLaunchUseCase()
            _uiState.value = _uiState.value.copy(
                isFirstLaunch = firstLaunch,
                isOnboardingCompleted = !firstLaunch
            )
        }
    }

    fun setCurrentPage(page: Int) {
        _uiState.value = _uiState.value.copy(currentPage = page)
    }

    fun completeOnboarding() {
        completeOnboardingUseCase()
        _uiState.value = _uiState.value.copy(isOnboardingCompleted = true)
    }
}
