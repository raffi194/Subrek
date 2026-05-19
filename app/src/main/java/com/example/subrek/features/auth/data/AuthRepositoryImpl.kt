package com.example.subrek.features.auth.data

import com.example.subrek.features.auth.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable
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
        val session = (supabaseClient.auth.sessionStatus.value as? SessionStatus.Authenticated)?.session
            ?: return com.example.subrek.features.auth.domain.model.UserProfile(
                id = "", email = "", fullName = null, avatarUrl = null
            )
        val userId = session.user?.id ?: ""
        val email = session.user?.email ?: ""

        return try {
            val result = supabaseClient.postgrest["profiles"]
                .select {
                    filter { eq("id", userId) }
                }
                .decodeSingleOrNull<ProfileDto>()

            com.example.subrek.features.auth.domain.model.UserProfile(
                id = userId,
                email = result?.email ?: email,
                fullName = result?.fullName,
                avatarUrl = result?.avatarUrl
            )
        } catch (e: Exception) {
            com.example.subrek.features.auth.domain.model.UserProfile(
                id = userId,
                email = email,
                fullName = null,
                avatarUrl = null
            )
        }
    }

    override suspend fun updateProfileFields(fullName: String, avatarUrl: String?): Result<Unit> = runCatching {
        val session = (supabaseClient.auth.sessionStatus.value as? SessionStatus.Authenticated)?.session
            ?: throw Exception("User tidak terautentikasi")
        val userId = session.user?.id ?: throw Exception("User ID tidak ditemukan")

        supabaseClient.postgrest["profiles"].update(
            {
                set("full_name", fullName)
                if (avatarUrl != null) set("avatar_url", avatarUrl)
                set("updated_at", "now()")
            }
        ) {
            filter { eq("id", userId) }
        }
    }

    override suspend fun uploadAvatarToStorage(uri: android.net.Uri): String {
        // Placeholder — implementasi upload ke Supabase Storage bucket 'avatars'
        // Untuk saat ini kembalikan string kosong agar tidak crash
        return ""
    }
}

@Serializable
private data class ProfileDto(
    @kotlinx.serialization.SerialName("id") val id: String,
    @kotlinx.serialization.SerialName("email") val email: String? = null,
    @kotlinx.serialization.SerialName("full_name") val fullName: String? = null,
    @kotlinx.serialization.SerialName("avatar_url") val avatarUrl: String? = null
)
