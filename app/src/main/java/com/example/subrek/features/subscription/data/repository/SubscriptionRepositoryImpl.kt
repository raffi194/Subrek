package com.example.subrek.features.subscription.data.repository

import com.example.subrek.features.subscription.data.local.LocalAppEntity
import com.example.subrek.features.subscription.data.local.LocalCategoryEntity
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

    override suspend fun getGhostSubscriptions(): List<Subscription> {
        return subscriptionDao.getGhostSubscriptions().map { it.toDomain() }
    }

    override fun getTotalMonthlyExpense(): Flow<Double> {
        return subscriptionDao.getTotalMonthlyExpense().map { it ?: 0.0 }
    }

    override fun getActiveSubscriptions(): Flow<List<Subscription>> {
        return subscriptionDao.getAllSubscriptions().map { entities ->
            entities.filter { it.status == "ACTIVE" || it.status == "TRIAL" }.map { it.toDomain() }
        }
    }

    override fun getSubscriptionHistory(): Flow<List<Subscription>> {
        return subscriptionDao.getSubscriptionHistory().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAverageConsumption(): Flow<Double> {
        return subscriptionDao.getAverageMonthlyConsumption()
    }

    override suspend fun deleteSubscriptionFromLocalAndRemote(id: String) {
        // 1. Hapus di lokal
        subscriptionDao.deleteSubscriptionById(id)
        
        // 2. Hapus di remote
        try {
            val session = supabaseClient.auth.currentSessionOrNull()
            val userId = session?.user?.id
            if (userId != null) {
                supabaseClient.postgrest["subscriptions"].delete {
                    filter {
                        eq("id", id)
                        eq("user_id", userId)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Jika gagal remote, kita biarkan saja dulu karena sudah terhapus di lokal
            // Idealnya ada mekanisme sync delete
        }
    }

    override fun getSubscriptionByIdFlow(id: String): Flow<Subscription?> {
        return subscriptionDao.getSubscriptionByIdFlow(id).map { it?.toDomain() }
    }

    override suspend fun updateSubscriptionBilling(
        id: String,
        price: Double,
        billingCycle: String,
        startDate: String
    ) {
        // Update lokal
        subscriptionDao.updateSubscriptionBilling(id, price, billingCycle, startDate)
        
        // Update remote (Push update)
        try {
            val session = supabaseClient.auth.currentSessionOrNull()
            val userId = session?.user?.id
            if (userId != null) {
                supabaseClient.postgrest["subscriptions"].update(
                    {
                        set("price", price)
                        set("billing_cycle", billingCycle)
                        set("start_date", startDate)
                        set("updated_at", "now()")
                    }
                ) {
                    filter {
                        eq("id", id)
                        eq("user_id", userId)
                        eq("status", "ACTIVE")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun terminateSubscription(id: String) {
        // Update lokal
        subscriptionDao.terminateSubscription(id)

        // Update remote
        try {
            val session = supabaseClient.auth.currentSessionOrNull()
            val userId = session?.user?.id
            if (userId != null) {
                supabaseClient.postgrest["subscriptions"].update(
                    {
                        set("status", "ENDED")
                        set("updated_at", "now()")
                    }
                ) {
                    filter {
                        eq("id", id)
                        eq("user_id", userId)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- Implementation Step 5.4 ---

    override suspend fun insertCategory(category: LocalCategoryEntity) {
        // 1. Simpan lokal terlebih dahulu (offline-first)
        subscriptionDao.insertCategory(category)

        // 2. Push ke Supabase cloud
        try {
            val session = supabaseClient.auth.currentSessionOrNull()
            val userId = session?.user?.id ?: return

            supabaseClient.postgrest["user_categories"].upsert(
                mapOf(
                    "id" to category.id,
                    "user_id" to userId,
                    "name" to category.name
                )
            )
        } catch (e: Exception) {
            // Gagal sync tidak menghalangi operasi lokal (offline-first)
            e.printStackTrace()
        }
    }

    override fun getCustomCategories(): Flow<List<LocalCategoryEntity>> {
        return subscriptionDao.getCustomCategories()
    }

    override suspend fun insertCustomApp(app: LocalAppEntity) {
        // 1. Simpan lokal terlebih dahulu (offline-first)
        subscriptionDao.insertCustomApp(app)

        // 2. Push ke Supabase cloud
        try {
            val session = supabaseClient.auth.currentSessionOrNull()
            val userId = session?.user?.id ?: return

            supabaseClient.postgrest["user_apps"].upsert(
                mapOf(
                    "id" to app.id,
                    "user_id" to userId,
                    "name" to app.name,
                    "icon_url" to app.iconUrl,
                    "category_name" to app.categoryName
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getCustomApps(): Flow<List<LocalAppEntity>> {
        return subscriptionDao.getCustomApps()
    }

    override suspend fun saveSubscription(
        name: String,
        iconUrl: String?,
        price: Double,
        cycle: String,
        date: String,
        isTrial: Boolean
    ) {
        val id = java.util.UUID.randomUUID().toString()
        val status = if (isTrial) "TRIAL" else "ACTIVE"
        val subscription = com.example.subrek.features.subscription.domain.model.Subscription(
            id = id,
            name = name,
            price = price,
            currency = "IDR",
            billingCycle = com.example.subrek.features.subscription.domain.model.BillingCycle.valueOf(cycle),
            startDate = LocalDate.parse(date),
            nextPaymentDate = LocalDate.parse(date), // Simplification: next payment is start date for now
            category = "Other",
            paymentMethod = "Manual",
            isTrial = isTrial,
            status = com.example.subrek.features.subscription.domain.model.SubscriptionStatus.valueOf(status),
            createdAt = LocalDate.now(),
            updatedAt = LocalDate.now()
        )
        insertSubscription(subscription)
    }
}
