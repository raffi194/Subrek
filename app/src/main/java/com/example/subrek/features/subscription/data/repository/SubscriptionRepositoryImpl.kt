package com.example.subrek.features.subscription.data.repository

import com.example.subrek.features.subscription.data.local.SubscriptionDao
import com.example.subrek.features.subscription.data.mapper.toDomain
import com.example.subrek.features.subscription.data.mapper.toDto
import com.example.subrek.features.subscription.data.mapper.toEntity
import com.example.subrek.features.subscription.data.remote.SubscriptionDto
import com.example.subrek.features.subscription.domain.model.Subscription
import com.example.subrek.features.subscription.domain.repository.SubscriptionRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepositoryImpl @Inject constructor(
    private val subscriptionDao: SubscriptionDao,
    private val supabaseClient: SupabaseClient
) : SubscriptionRepository {

    override fun getAllSubscriptions(): Flow<List<Subscription>> {
        return subscriptionDao.getAllSubscriptions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getSubscriptionById(id: String): Subscription? {
        return subscriptionDao.getSubscriptionById(id)?.toDomain()
    }

    override suspend fun insertSubscription(subscription: Subscription) {
        // Simpan ke lokal dengan flag isDirty = true agar bisa di-sync kemudian
        subscriptionDao.insertSubscription(subscription.toEntity(isDirty = true))
    }

    override suspend fun deleteSubscription(id: String) {
        subscriptionDao.deleteSubscriptionById(id)
    }

    override suspend fun syncWithRemote(): Result<Unit> {
        return try {
            val session = supabaseClient.auth.currentSessionOrNull()
            val userId = session?.user?.id ?: return Result.failure(Exception("User not authenticated"))

            // 1. PUSH PHASE: Unggah data lokal yang "dirty" (berubah saat offline)
            val dirtyEntities = subscriptionDao.getDirtySubscriptions()
            if (dirtyEntities.isNotEmpty()) {
                val dtosToUpload = dirtyEntities.map { it.toDto(userId) }
                
                // Upsert ke Supabase cloud (Insert atau Update berdasarkan Primary Key ID)
                supabaseClient.postgrest["subscriptions"].upsert(dtosToUpload)
                
                // Setelah sukses diunggah, matikan flag dirty di database lokal
                val cleanedEntities = dirtyEntities.map { it.copy(isDirty = false) }
                subscriptionDao.insertSubscriptions(cleanedEntities)
            }

            // 2. PULL PHASE: Ambil data terbaru dari cloud Supabase
            val response = supabaseClient.postgrest["subscriptions"]
                .select(columns = Columns.ALL) {
                    filter {
                        eq("user_id", userId)
                    }
                }
            
            val remoteDtos = response.decodeList<SubscriptionDto>()

            if (remoteDtos.isNotEmpty()) {
                // Ubah DTO dari cloud menjadi entity Room lokal
                val localEntities = remoteDtos.map { it.toEntity() }
                
                // Simpan/SINKRONKAN ke SQLite lokal
                subscriptionDao.insertSubscriptions(localEntities)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            // Log galat jika koneksi internet terputus atau API bermasalah.
            // Arsitektur Offline-First menjamin aplikasi tetap bisa beroperasi menggunakan cache lokal.
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getSubscriptionsExpiringInDays(days: Int): List<Subscription> {
        val targetLocalDate = LocalDate.now().plusDays(days.toLong())
        val formattedTarget = targetLocalDate.format(DateTimeFormatter.ISO_LOCAL_DATE) // "YYYY-MM-DD"

        return subscriptionDao.getSubscriptionsExpiringOn(formattedTarget).map {
            it.toDomain()
        }
    }
}
