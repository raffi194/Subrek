package com.example.subrek.features.subscription.domain.repository

import com.example.subrek.features.subscription.domain.model.Subscription
import kotlinx.coroutines.flow.Flow
import kotlin.Result

/**
 * Kontrak abstraksi repositori untuk fitur langganan.
 * Lapisan presentation/viewmodel hanya akan bergantung pada interface ini.
 */
interface SubscriptionRepository {
    
    /**
     * Mengambil seluruh daftar langganan pengguna dalam bentuk reaktif Flow.
     */
    fun getAllSubscriptions(): Flow<List<Subscription>>
    
    /**
     * Mengambil detail satu langganan spesifik berdasarkan ID.
     */
    suspend fun getSubscriptionById(id: String): Subscription?
    
    /**
     * Menyimpan atau memperbarui data langganan ke penyimpanan lokal.
     */
    suspend fun insertSubscription(subscription: Subscription)
    
    /**
     * Menghapus pencatatan langganan berdasarkan ID.
     */
    suspend fun deleteSubscription(id: String)
    
    /**
     * Memicu proses sinkronisasi latar belakang antara Room lokal dan cloud Supabase.
     */
    suspend fun syncWithRemote(): Result<Unit>

    /**
     * Mengambil daftar langganan yang akan jatuh tempo dalam jumlah hari tertentu.
     */
    suspend fun getSubscriptionsExpiringInDays(days: Int): List<Subscription>

    /**
     * Mengambil daftar langganan yang terdeteksi sebagai "Ghost Subscription" (tidak dikonfirmasi >= 2 siklus).
     */
    suspend fun getGhostSubscriptions(): List<Subscription>

    /**
     * Mengambil estimasi total pengeluaran bulanan.
     */
    fun getTotalMonthlyExpense(): Flow<Double>

    fun getActiveSubscriptions(): Flow<List<Subscription>>

    fun getSubscriptionHistory(): Flow<List<Subscription>>

    fun getAverageConsumption(): Flow<Double>

    fun getActiveSubscriptionsCount(): Flow<Int>

    suspend fun deleteSubscriptionFromLocalAndRemote(id: String)

    fun getSubscriptionByIdFlow(id: String): Flow<Subscription?>

    suspend fun updateSubscriptionBilling(
        id: String,
        price: Double,
        billingCycle: String,
        startDate: String,
        paymentMethod: String,
        isTrial: Boolean,
        status: String
    )

    suspend fun terminateSubscription(id: String)

    suspend fun insertCustomApp(app: com.example.subrek.features.subscription.data.local.LocalAppEntity)
    fun getCatalogItemsFlow(): Flow<List<com.example.subrek.features.subscription.domain.model.CatalogItem>>
    suspend fun saveSubscription(name: String, iconUrl: String?, price: Double, cycle: String, date: String, isTrial: Boolean)

    suspend fun saveSubscriptionExtended(
        id: String,
        name: String,
        price: Double,
        currency: String,
        billingCycle: String,
        paymentMethod: String,
        nextPaymentDate: String,
        status: String,
        iconUrl: String? = null
    )

    suspend fun uploadAppIconStorage(uri: android.net.Uri): String?
    suspend fun deleteCustomApp(id: String)}
