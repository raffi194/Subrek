package com.example.subrek.features.subscription.data.mapper

import com.example.subrek.features.subscription.data.local.SubscriptionEntity
import com.example.subrek.features.subscription.data.remote.SubscriptionDto
import com.example.subrek.features.subscription.domain.model.BillingCycle
import com.example.subrek.features.subscription.domain.model.Subscription
import com.example.subrek.features.subscription.domain.model.SubscriptionStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

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
        status = SubscriptionStatus.valueOf(status),
        unconfirmedCyclesCount = unconfirmedCyclesCount,
        createdAt = LocalDate.parse(createdAt, dateFormatter),
        updatedAt = LocalDate.now(),
        iconUrl = iconUrl
    )
}

// 2. Dari Pure Domain Model ke Room Entity (Mark as Dirty for local changes)
fun Subscription.toEntity(isDirty: Boolean = true): SubscriptionEntity {
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
        unconfirmedCyclesCount = unconfirmedCyclesCount,
        createdAt = createdAt.format(dateFormatter),
        isDirty = isDirty,
        updatedAt = System.currentTimeMillis(),
        iconUrl = iconUrl
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
        nextPaymentDate = nextPaymentDate,
        category = category,
        paymentMethod = paymentMethod,
        isTrial = isTrial,
        isGhostSubscription = isGhostSubscription,
        status = status,
        unconfirmedCyclesCount = unconfirmedCyclesCount,
        createdAt = createdAt,
        iconUrl = iconUrl
    )
}

// 4. Dari Supabase DTO ke Room Entity (Mark as Clean)
fun SubscriptionDto.toEntity(): SubscriptionEntity {
    return SubscriptionEntity(
        id = id,
        name = name,
        price = price,
        currency = currency,
        billingCycle = billingCycle,
        startDate = nextPaymentDate, // Menggunakan nextPaymentDate sebagai fallback karena cloud tidak menyimpan startDate
        nextPaymentDate = nextPaymentDate,
        category = category,
        paymentMethod = paymentMethod,
        isTrial = isTrial,
        isGhostSubscription = isGhostSubscription,
        status = status,
        unconfirmedCyclesCount = unconfirmedCyclesCount,
        createdAt = createdAt,
        isDirty = false,
        updatedAt = System.currentTimeMillis(),
        iconUrl = iconUrl
    )
}
