package com.example.subrek.features.profile.presentation.viewmodel

data class ProfileUiState(
    val id: String = "",
    val email: String = "",             // Ditambahkan sesuai kolom database profiles
    val fullName: String? = null,
    val avatarUrl: String? = null,
    val isLoading: Boolean = false,
    val isUpdateSuccess: Boolean = false,
    val isLoggedOut: Boolean = false,
    val errorMessage: String? = null
) {
    // Memeriksa secara reaktif apakah ada perubahan data dibanding data asli
    // Catatan: originalName dan originalAvatarUrl dihapus karena kita menggunakan fullName dan avatarUrl langsung
}
