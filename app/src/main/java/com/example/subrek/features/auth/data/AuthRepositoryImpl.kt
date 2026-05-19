package com.example.subrek.features.auth.data

import com.example.subrek.features.auth.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : AuthRepository {

    override val currentUserSession: Flow<String?> = supabaseClient.auth.sessionStatus
        .map { status ->
            when (status) {
                is SessionStatus.Authenticated -> status.session.user?.id
                else -> null
            }
        }

    override suspend fun signUp(email: String, password: String): Result<Unit> = runCatching {
        supabaseClient.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        supabaseClient.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        supabaseClient.auth.signOut()
    }

    override suspend fun getCurrentUserProfile(): com.example.subrek.features.auth.domain.model.UserProfile {
        // Mock implementation for now to satisfy the interface
        return com.example.subrek.features.auth.domain.model.UserProfile(
            id = "user-123",
            email = "user@example.com",
            fullName = "John Doe",
            avatarUrl = null
        )
    }

    override suspend fun updateProfileFields(fullName: String, avatarUrl: String?): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun uploadAvatarToStorage(uri: android.net.Uri): String {
        return ""
    }
}
