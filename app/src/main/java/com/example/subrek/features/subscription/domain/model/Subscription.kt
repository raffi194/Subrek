package com.example.subrek.features.subscription.domain.model

import java.util.Date

data class Subscription(
    val id: String,
    val name: String,
    val price: Double,
    val billingCycle: String, // Monthly, Yearly
    val nextBillingDate: Date,
    val category: String,
    val iconUrl: String? = null
)
