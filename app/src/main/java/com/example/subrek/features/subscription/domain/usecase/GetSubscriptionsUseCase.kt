package com.example.subrek.features.subscription.domain.usecase

import com.example.subrek.features.subscription.domain.model.Subscription
import com.example.subrek.features.subscription.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSubscriptionsUseCase @Inject constructor(
    private val repository: SubscriptionRepository
) {
    operator fun invoke(): Flow<List<Subscription>> {
        return repository.getAllSubscriptions()
    }
}
