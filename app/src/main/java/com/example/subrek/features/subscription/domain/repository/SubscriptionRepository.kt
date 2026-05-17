package com.example.subrek.features.subscription.domain.repository

import com.example.subrek.features.subscription.domain.model.Subscription
import kotlinx.coroutines.flow.Flow

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
}
