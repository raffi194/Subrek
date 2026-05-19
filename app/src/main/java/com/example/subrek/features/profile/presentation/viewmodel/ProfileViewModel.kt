package com.example.subrek.features.profile.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subrek.features.auth.domain.repository.AuthRepository
import com.example.subrek.features.subscription.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val userProfile = authRepository.getCurrentUserProfile()
                _uiState.update { it.copy(
                    id = userProfile.id,
                    email = userProfile.email,
                    fullName = userProfile.fullName,
                    avatarUrl = userProfile.avatarUrl,
                    isLoading = false
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun updateProfileData(newFullName: String, newImageUri: Uri?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                var finalAvatarUrl = _uiState.value.avatarUrl

                // 1. Jika ada file gambar baru yang di-pick, upload ke Supabase Storage Bucket
                if (newImageUri != null) {
                    finalAvatarUrl = authRepository.uploadAvatarToStorage(newImageUri)
                }

                // 2. Update kolom full_name dan avatar_url ke database public.profiles
                authRepository.updateProfileFields(
                    fullName = newFullName,
                    avatarUrl = finalAvatarUrl
                ).onSuccess {
                    _uiState.update { it.copy(
                        fullName = newFullName,
                        avatarUrl = finalAvatarUrl,
                        isLoading = false,
                        isUpdateSuccess = true
                    )}
                }.onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun resetUpdateStatus() {
        _uiState.update { it.copy(isUpdateSuccess = false, errorMessage = null) }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.signOut().onSuccess {
                _uiState.update { it.copy(isLoggedOut = true, isLoading = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage) }
            }
        }
    }

    fun navigateToEdit() {
        _uiState.update { it.copy(navigateToEdit = true) }
    }

    fun onEditNavigated() {
        _uiState.update { it.copy(navigateToEdit = false) }
    }
}
