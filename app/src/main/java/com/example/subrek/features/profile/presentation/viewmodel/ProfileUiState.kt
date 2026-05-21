package com.example.subrek.features.profile.presentation.viewmodel

data class ProfileUiState(
    val id: String = "",
    val email: String = "",
    val fullName: String? = null,
    val avatarUrl: String? = null,
    val isLoading: Boolean = false,
    val isUpdateSuccess: Boolean = false,
    val isLoggedOut: Boolean = false,
    val errorMessage: String? = null,
    val navigateToEdit: Boolean = false
) {
}
