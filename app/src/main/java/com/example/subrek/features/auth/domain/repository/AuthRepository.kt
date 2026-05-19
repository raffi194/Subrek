import android.net.Uri
import com.example.subrek.features.auth.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserSession: Flow<String?> // Mengembalikan User ID jika sesi aktif
    suspend fun signUp(email: String, password: String): Result<Unit>
    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun signOut(): Result<Unit>
    
    suspend fun getCurrentUserProfile(): UserProfile
    suspend fun updateProfileFields(fullName: String, avatarUrl: String?): Result<Unit>
    suspend fun uploadAvatarToStorage(uri: Uri): String
}
