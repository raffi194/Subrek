package com.example.subrek.features.subscription.data.mapper

import com.example.subrek.features.subscription.data.local.SubscriptionEntity
import com.example.subrek.features.subscription.domain.model.BillingCycle
import com.example.subrek.features.subscription.domain.model.Subscription
import com.example.subrek.features.subscription.domain.model.SubscriptionStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

fun SubscriptionEntity.toDomain(): Subscription {
    return Subscription(
        id = id,
        name = name,
        price = price,
        currency = currency,
        billingCycle = BillingCycle.valueOf(billingCycle),
        startDate = LocalDate.parse(startDate, dateFormatter),
        nextPaymentDate = LocalDate.parse(nextPaymentDate, dateFormatter),
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

fun Subscription.toEntity(): SubscriptionEntity {
    return SubscriptionEntity(
        id = id,
        name = name,
        price = price,
        currency = currency,
        billingCycle = billingCycle.name,
        startDate = startDate.format(dateFormatter),
        nextPaymentDate = nextPaymentDate.format(dateFormatter),
        paymentMethod = paymentMethod,
        isTrial = isTrial,
        isGhostSubscription = isGhostSubscription,
        status = status.name,
        unconfirmedCyclesCount = unconfirmedCyclesCount,
        createdAt = createdAt.format(dateFormatter),
        updatedAt = System.currentTimeMillis(),
        iconUrl = iconUrl
    )
}