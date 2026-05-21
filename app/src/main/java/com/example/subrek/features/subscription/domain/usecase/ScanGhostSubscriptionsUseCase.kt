package com.example.subrek.features.subscription.domain.usecase

import com.example.subrek.features.subscription.domain.model.Subscription
import com.example.subrek.features.subscription.domain.model.SubscriptionStatus
import com.example.subrek.features.subscription.domain.repository.SubscriptionRepository
import javax.inject.Inject

class ScanGhostSubscriptionsUseCase @Inject constructor(
    private val repository: SubscriptionRepository
) {
    suspend operator fun invoke(subscriptions: List<Subscription>): Int {
        var detectedCount = 0
        subscriptions.forEach { sub ->
            if (sub.unconfirmedCyclesCount >= 2 && sub.status != SubscriptionStatus.NEEDS_REVIEW) {
                repository.insertSubscription(sub.copy(status = SubscriptionStatus.NEEDS_REVIEW))
                detectedCount++
            }
        }
        if (detectedCount > 0) {
            repository.syncWithRemote()
        }
        return detectedCount
    }
}
