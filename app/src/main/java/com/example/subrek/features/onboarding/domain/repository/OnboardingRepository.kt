package com.example.subrek.features.onboarding.domain.repository

interface OnboardingRepository {
    fun isFirstLaunch(): Boolean
    fun setOnboardingCompleted()
}
