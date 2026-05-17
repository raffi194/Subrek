package com.example.subrek.features.subscription.data.mapper

import com.example.subrek.features.subscription.data.local.SubscriptionEntity
import com.example.subrek.features.subscription.data.remote.SubscriptionDto
import com.example.subrek.features.subscription.domain.model.BillingCycle
import com.example.subrek.features.subscription.domain.model.Subscription
import com.example.subrek.features.subscription.domain.model.SubscriptionStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Kumpulan Extension Functions Mapper untuk konversi data tiga arah:
 * Domain Model <-> Room Entity <-> Supabase DTO
 */

private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE // Format: YYYY-MM-DD

// 1. Dari Room Entity ke Pure Domain Model
fun SubscriptionEntity.toDomain(): Subscription {
    return Subscription(
        id = id,
        name = name,
        price = price,
        currency = currency,
        billingCycle = BillingCycle.valueOf(billingCycle),
        startDate = LocalDate.parse(startDate, dateFormatter),
        nextPaymentDate = LocalDate.parse(nextPaymentDate, dateFormatter),
        category = category,
        paymentMethod = paymentMethod,
        isTrial = isTrial,
        isGhostSubscription = isGhostSubscription,
        status = SubscriptionStatus.valueOf(status)
    )
}

// 2. Dari Pure Domain Model ke Room Entity
fun Subscription.toEntity(): SubscriptionEntity {
    return SubscriptionEntity(
        id = id,
        name = name,
        price = price,
        currency = currency,
        billingCycle = billingCycle.name,
        startDate = startDate.format(dateFormatter),
        nextPaymentDate = nextPaymentDate.format(dateFormatter),
        category = category,
        paymentMethod = paymentMethod,
        isTrial = isTrial,
        isGhostSubscription = isGhostSubscription,
        status = status.name,
        updatedAt = System.currentTimeMillis()
    )
}

// 3. Dari Room Entity ke Supabase DTO
fun SubscriptionEntity.toDto(userId: String): SubscriptionDto {
    return SubscriptionDto(
        id = id,
        userId = userId,
        name = name,
        price = price,
        currency = currency,
        billingCycle = billingCycle,
        startDate = startDate,
        nextPaymentDate = nextPaymentDate,
        category = category,
        paymentMethod = paymentMethod,
        isTrial = isTrial,
        isGhostSubscription = isGhostSubscription,
        status = status
    )
}

// 4. Dari Supabase DTO ke Room Entity
fun SubscriptionDto.toEntity(): SubscriptionEntity {
    return SubscriptionEntity(
        id = id,
        name = name,
        price = price,
        currency = currency,
        billingCycle = billingCycle,
        startDate = startDate,
        nextPaymentDate = nextPaymentDate,
        category = category,
        paymentMethod = paymentMethod,
        isTrial = isTrial,
        isGhostSubscription = isGhostSubscription,
        status = status,
        updatedAt = System.currentTimeMillis()
    )
}
