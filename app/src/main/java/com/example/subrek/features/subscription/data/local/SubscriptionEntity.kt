package com.example.subrek.features.subscription.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val price: Double,
    val billingCycle: String,
    val nextBillingDate: Long,
    val category: String,
    val iconUrl: String?
)
