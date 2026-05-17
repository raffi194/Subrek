package com.example.subrek.features.subscription.domain.repository

import com.example.subrek.features.subscription.domain.model.Subscription
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    fun getAllSubscriptions(): Flow<List<Subscription>>
    suspend fun addSubscription(subscription: Subscription)
    suspend fun deleteSubscription(id: String)
}
