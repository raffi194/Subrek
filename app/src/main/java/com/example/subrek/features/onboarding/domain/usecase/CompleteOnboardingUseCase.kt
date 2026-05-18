package com.example.subrek.features.onboarding.domain.usecase

import com.example.subrek.features.onboarding.domain.repository.OnboardingRepository
import javax.inject.Inject

class CompleteOnboardingUseCase @Inject constructor(
    private val repository: OnboardingRepository
) {
    operator fun invoke() = repository.setOnboardingCompleted()
}
