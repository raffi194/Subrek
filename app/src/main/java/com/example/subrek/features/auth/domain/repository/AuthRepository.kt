package com.example.subrek.features.auth.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserSession: Flow<String?> // Mengembalikan User ID jika sesi aktif
    suspend fun signUp(email: String, password: String): Result<Unit>
    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun signOut(): Result<Unit>
}
