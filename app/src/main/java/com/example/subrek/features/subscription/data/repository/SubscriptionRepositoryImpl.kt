package com.example.subrek.features.subscription.data.repository

import com.example.subrek.features.subscription.domain.model.Subscription
import com.example.subrek.features.subscription.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class SubscriptionRepositoryImpl @Inject constructor() : SubscriptionRepository {
    override fun getAllSubscriptions(): Flow<List<Subscription>> {
        // Implementation for Room/Remote will go here
        return flowOf(emptyList())
    }

    override suspend fun getSubscriptionById(id: String): Subscription? {
        // Implementation
        return null
    }

    override suspend fun insertSubscription(subscription: Subscription) {
        // Implementation
    }

    override suspend fun deleteSubscription(id: String) {
        // Implementation
    }

    override suspend fun syncWithRemote(): Result<Unit> {
        // Implementation
        return Result.success(Unit)
    }
}
