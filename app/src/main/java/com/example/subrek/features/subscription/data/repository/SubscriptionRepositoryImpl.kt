package com.example.subrek.features.subscription.data.repository

import com.example.subrek.features.subscription.data.local.LocalAppEntity
import com.example.subrek.features.subscription.data.local.SubscriptionDao
import com.example.subrek.features.subscription.data.mapper.toDomain
import com.example.subrek.features.subscription.data.mapper.toDto
import com.example.subrek.features.subscription.data.mapper.toEntity
import com.example.subrek.features.subscription.data.remote.SubscriptionDto
import com.example.subrek.features.subscription.domain.model.Subscription
import com.example.subrek.features.subscription.domain.repository.SubscriptionRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.catch
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
        subscriptionDao.insertSubscription(subscription.toEntity(isDirty = true))
    }

    override suspend fun deleteSubscription(id: String) {
        subscriptionDao.deleteSubscriptionById(id)
    }

    override suspend fun syncWithRemote(): Result<Unit> {
        return try {
            val session = (supabaseClient.auth.sessionStatus.value as? SessionStatus.Authenticated)?.session
            val userId = session?.user?.id ?: return Result.failure(Exception("User not authenticated"))

            val dirtyEntities = subscriptionDao.getDirtySubscriptions()
            if (dirtyEntities.isNotEmpty()) {
                val dtosToUpload = dirtyEntities.map { it.toDto(userId) }
                supabaseClient.postgrest["subscriptions"].upsert(dtosToUpload)
                val cleanedEntities = dirtyEntities.map { it.copy(isDirty = false) }
                subscriptionDao.insertSubscriptions(cleanedEntities)
            }

            val response = supabaseClient.postgrest["subscriptions"]
                .select(columns = Columns.ALL) {
                    filter {
                        eq("user_id", userId)
                    }
                }
            
            val remoteDtos = response.decodeList<SubscriptionDto>()

            if (remoteDtos.isNotEmpty()) {
                val localEntities = remoteDtos.map { it.toEntity() }
                subscriptionDao.insertSubscriptions(localEntities)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getSubscriptionsExpiringInDays(days: Int): List<Subscription> {
        val targetLocalDate = LocalDate.now().plusDays(days.toLong())
        val formattedTarget = targetLocalDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

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
            .map { it ?: 0.0 }
            .catch { emit(0.0) }
    }

    override fun getActiveSubscriptionsCount(): Flow<Int> {
        return subscriptionDao.getAllSubscriptions().map { entities ->
            entities.count { it.status == "ACTIVE" || it.status == "TRIAL" }
        }.catch { emit(0) }
    }

    override suspend fun deleteSubscriptionFromLocalAndRemote(id: String) {
        subscriptionDao.deleteSubscriptionById(id)
        try {
            val session = (supabaseClient.auth.sessionStatus.value as? SessionStatus.Authenticated)?.session
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
        subscriptionDao.updateSubscriptionBilling(id, price, billingCycle, startDate)
        try {
            val session = (supabaseClient.auth.sessionStatus.value as? SessionStatus.Authenticated)?.session
            val userId = session?.user?.id
            if (userId != null) {
                supabaseClient.postgrest.from("subscriptions").update(
                    {
                        set("price", price)
                        set("billing_cycle", billingCycle)
                        set("next_payment_date", startDate) 
                        set("updated_at", java.time.Instant.now().toString())
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
        subscriptionDao.terminateSubscription(id)
        try {
            val session = (supabaseClient.auth.sessionStatus.value as? SessionStatus.Authenticated)?.session
            val userId = session?.user?.id
            if (userId != null) {
                supabaseClient.postgrest.from("subscriptions").update(
                    {
                        set("status", "ENDED")
                        set("updated_at", java.time.Instant.now().toString())
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

    override suspend fun insertCustomApp(app: LocalAppEntity) {
        subscriptionDao.insertCustomApp(app)
        try {
            val session = (supabaseClient.auth.sessionStatus.value as? SessionStatus.Authenticated)?.session
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
        saveSubscriptionExtended(
            id = id,
            name = name,
            price = price,
            currency = "IDR",
            billingCycle = cycle,
            paymentMethod = "Manual",
            nextPaymentDate = date,
            status = if (isTrial) "TRIAL" else "ACTIVE",
            iconUrl = iconUrl
        )
    }

    override suspend fun saveSubscriptionExtended(
        id: String,
        name: String,
        price: Double,
        currency: String,
        billingCycle: String,
        paymentMethod: String,
        nextPaymentDate: String,
        status: String,
        iconUrl: String?
    ) {
        val session = (supabaseClient.auth.sessionStatus.value as? SessionStatus.Authenticated)?.session
        val userId = session?.user?.id

        val subscription = com.example.subrek.features.subscription.domain.model.Subscription(
            id = id,
            name = name,
            price = price,
            currency = currency,
            billingCycle = com.example.subrek.features.subscription.domain.model.BillingCycle.valueOf(billingCycle),
            startDate = LocalDate.parse(nextPaymentDate),
            nextPaymentDate = LocalDate.parse(nextPaymentDate),
            paymentMethod = paymentMethod,
            isTrial = status == "TRIAL",
            status = com.example.subrek.features.subscription.domain.model.SubscriptionStatus.valueOf(status),
            createdAt = LocalDate.now(),
            updatedAt = LocalDate.now(),
            iconUrl = iconUrl
        )
        
        insertSubscription(subscription)

        if (userId != null) {
            try {
                val dto = SubscriptionDto(
                    id = id,
                    userId = userId,
                    name = name,
                    price = price,
                    currency = currency,
                    billingCycle = billingCycle,
                    nextPaymentDate = nextPaymentDate,
                    category = "Other",
                    paymentMethod = paymentMethod,
                    isTrial = status == "TRIAL",
                    isGhostSubscription = false,
                    status = status,
                    createdAt = LocalDate.now().toString(),
                    iconUrl = iconUrl
                )
                supabaseClient.postgrest["subscriptions"].insert(dto)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun uploadAppIconStorage(uri: android.net.Uri): String? {
        return try {
            val session = (supabaseClient.auth.sessionStatus.value as? SessionStatus.Authenticated)?.session
            val userId = session?.user?.id ?: return null
            "https://placeholder.co/100"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
