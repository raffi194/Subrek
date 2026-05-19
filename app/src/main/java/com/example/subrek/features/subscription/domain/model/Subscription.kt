package com.example.subrek.features.subscription.domain.model

import java.time.LocalDate

/**
 * Pure Kotlin Entity untuk data Langganan (Subscription).
 * Berkas ini terisolasi dari anotasi framework database (Room) maupun remote (Supabase).
 */
data class Subscription(
    val id: String,
    val name: String,
    val price: Double,
    val currency: String,
    val billingCycle: BillingCycle, // WEEKLY, MONTHLY, YEARLY
    val startDate: LocalDate,
    val nextPaymentDate: LocalDate,
    val paymentMethod: String,
    val isTrial: Boolean,
    val isGhostSubscription: Boolean = false,
    val status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    val unconfirmedCyclesCount: Int = 0, 
    val createdAt: LocalDate,
    val updatedAt: LocalDate,
    val iconUrl: String? = null
)

enum class BillingCycle {
    WEEKLY,
    MONTHLY,
    YEARLY
}

enum class SubscriptionStatus {
    ACTIVE,
    TRIAL,
    GRACE_PERIOD,
    PAUSED,
    CANCELLED,
    NEEDS_REVIEW
}
