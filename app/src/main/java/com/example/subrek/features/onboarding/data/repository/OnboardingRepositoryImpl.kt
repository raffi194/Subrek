package com.example.subrek.features.onboarding.data.repository

import android.content.SharedPreferences
import com.example.subrek.features.onboarding.domain.repository.OnboardingRepository
import javax.inject.Inject

class OnboardingRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : OnboardingRepository {

    companion object {
        private const val KEY_FIRST_LAUNCH = "key_first_launch"
    }

    override fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    override fun setOnboardingCompleted() {
        sharedPreferences.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }
}
