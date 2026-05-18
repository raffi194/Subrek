package com.example.subrek.features.subscription.domain.usecase

import com.example.subrek.features.subscription.domain.model.Subscription
import com.example.subrek.features.subscription.domain.repository.SubscriptionRepository
import javax.inject.Inject

class AddSubscriptionUseCase @Inject constructor(
    private val repository: SubscriptionRepository
) {
    suspend operator fun invoke(subscription: Subscription) {
        repository.insertSubscription(subscription)
    }
}
