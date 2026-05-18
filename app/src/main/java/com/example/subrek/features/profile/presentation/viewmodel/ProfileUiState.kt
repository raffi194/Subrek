package com.example.subrek.features.profile.presentation.viewmodel

data class ProfileUiState(
    val id: String = "",
    val originalName: String = "",
    val currentName: String = "",
    val email: String = "",
    val originalAvatarUrl: String = "",
    val currentAvatarUrl: String = "",
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isLoggedOut: Boolean = false,
    val errorMessage: String? = null
) {
    // Memeriksa secara reaktif apakah ada perubahan data dibanding data asli
    val hasChanges: Boolean 
        get() = currentName != originalName || currentAvatarUrl != originalAvatarUrl
}
