package com.example.subrek.features.profile.presentation.viewmodel

import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    // Menggunakan SharedPreferences untuk menyimpan data profil secara lokal
    private val sharedPreferences: SharedPreferences
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
                // 1. Ambil data profil dari SharedPreferences (Penyimpanan Lokal)
                val localName = sharedPreferences.getString("pref_full_name", "Pengguna Subrek") ?: "Pengguna Subrek"
                val localAvatar = sharedPreferences.getString("pref_avatar_url", null)

                _uiState.update { it.copy(
                    id = "local_user", // ID Dummy
                    email = "offline@subrek.app", // Email dummy atau bisa dihapus dari UI
                    fullName = localName,
                    avatarUrl = localAvatar,
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

                // 2. Gunakan URI lokal langsung sebagai string (Tidak diunggah ke internet)
                if (newImageUri != null) {
                    finalAvatarUrl = newImageUri.toString()
                }

                // 3. Simpan perubahan ke SharedPreferences
                sharedPreferences.edit().apply {
                    putString("pref_full_name", newFullName)
                    putString("pref_avatar_url", finalAvatarUrl)
                    apply()
                }

                _uiState.update { it.copy(
                    fullName = newFullName,
                    avatarUrl = finalAvatarUrl,
                    isLoading = false,
                    isUpdateSuccess = true
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun resetUpdateStatus() {
        _uiState.update { it.copy(isUpdateSuccess = false, errorMessage = null) }
    }

    fun navigateToEdit() {
        _uiState.update { it.copy(navigateToEdit = true) }
    }

    fun onEditNavigated() {
        _uiState.update { it.copy(navigateToEdit = false) }
    }

    // FUNGSI LOGOUT TELAH DIHAPUS
}