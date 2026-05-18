package com.example.subrek.features.subscription.domain.usecase

import com.example.subrek.features.subscription.domain.model.Subscription
import com.example.subrek.features.subscription.domain.model.SubscriptionStatus
import com.example.subrek.features.subscription.domain.repository.SubscriptionRepository
import javax.inject.Inject

/**
 * Use case untuk memindai langganan yang berpotensi menjadi "Ghost Subscription".
 * Algoritma ini menandai layanan jika jumlah siklus tidak dikonfirmasi >= 2.
 */
class ScanGhostSubscriptionsUseCase @Inject constructor(
    private val repository: SubscriptionRepository
) {
    // ALGORITMA GHOST DETECTOR: Otomatis menandai layanan jika status unconfirmed >= 2 siklus
    suspend operator fun invoke(subscriptions: List<Subscription>): Int {
        var detectedCount = 0
        subscriptions.forEach { sub ->
            if (sub.unconfirmedCyclesCount >= 2 && sub.status != SubscriptionStatus.NEEDS_REVIEW) {
                // Picu repository untuk memperbarui status entitas lokal ke Room
                repository.insertSubscription(sub.copy(status = SubscriptionStatus.NEEDS_REVIEW))
                detectedCount++
            }
        }
        if (detectedCount > 0) {
            // Sinkronkan perubahan status ke cloud Supabase jika ada deteksi
            repository.syncWithRemote()
        }
        return detectedCount
    }
}
