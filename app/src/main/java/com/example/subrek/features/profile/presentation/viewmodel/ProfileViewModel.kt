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
    private val authRepository: AuthRepository,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            // Simulasi pengambilan data profil. 
            // Pada tahap berikutnya, ini akan mengambil data asli dari Supabase/Room.
            _uiState.update { 
                it.copy(
                    id = "user-123",
                    originalName = "Muhamad Raffi",
                    currentName = "Muhamad Raffi",
                    email = "raffi.dev@example.com",
                    originalAvatarUrl = "https://placeholder.co/150",
                    currentAvatarUrl = "https://placeholder.co/150"
                )
            }
        }
    }

    fun toggleEditMode(enabled: Boolean) {
        _uiState.update { it.copy(isEditMode = enabled) }
    }

    fun updateName(newName: String) {
        _uiState.update { it.copy(currentName = newName) }
    }

    fun updateAvatarUri(uri: Uri) {
        // Uri dikonversi menjadi string untuk pratinjau lokal sebelum diunggah
        _uiState.update { it.copy(currentAvatarUrl = uri.toString()) }
    }

    fun saveProfileChanges() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val state = _uiState.value
                // TODO: Implementasi repository.updateProfile(state.id, state.currentName, state.currentAvatarUrl)
                
                _uiState.update { 
                    it.copy(
                        originalName = state.currentName,
                        originalAvatarUrl = state.currentAvatarUrl,
                        isEditMode = false,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage) }
            }
        }
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
}
