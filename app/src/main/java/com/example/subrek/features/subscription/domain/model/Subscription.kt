package com.example.subrek.features.subscription.domain.model

import java.time.LocalDate

data class Subscription(
    val id: String,
    val name: String,
    val price: Double,
    val currency: String,
    val billingCycle: BillingCycle,
    val startDate: LocalDate,
    val nextPaymentDate: LocalDate,
    val paymentMethod: String,
    val isTrial: Boolean,
    val isGhostSubscription: Boolean = false,
    val status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    val unconfirmedCyclesCount: Int = 0,
    val createdAt: LocalDate,
    val updatedAt: LocalDate,
    val iconUrl: String? = null,
    val confirmedPaymentDates: String = ""
) {
    fun getUnconfirmedPaymentDates(today: LocalDate = LocalDate.now()): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        val start = this.startDate
        
        when (this.billingCycle.name) {
            "WEEKLY" -> {
                var current = start
                while (!current.isAfter(today)) {
                    val dateStr = current.toString()
                    val isConfirmed = this.confirmedPaymentDates.split(",").contains(dateStr)
                    if (!isConfirmed) {
                        dates.add(current)
                    }
                    current = current.plusWeeks(1)
                }
            }
            "MONTHLY" -> {
                var current = start
                while (!current.isAfter(today)) {
                    val dateStr = current.toString()
                    val isConfirmed = this.confirmedPaymentDates.split(",").contains(dateStr)
                    if (!isConfirmed) {
                        dates.add(current)
                    }
                    current = current.plusMonths(1)
                }
            }
            "YEARLY" -> {
                var current = start
                while (!current.isAfter(today)) {
                    val dateStr = current.toString()
                    val isConfirmed = this.confirmedPaymentDates.split(",").contains(dateStr)
                    if (!isConfirmed) {
                        dates.add(current)
                    }
                    current = current.plusYears(1)
                }
            }
        }
        return dates
    }

    fun confirmPaymentDate(dateToConfirm: LocalDate): Subscription {
        val dateStr = dateToConfirm.toString()
        val list = this.confirmedPaymentDates.split(",").filter { it.isNotBlank() }.toMutableList()
        if (!list.contains(dateStr)) {
            list.add(dateStr)
        }
        
        var nextDate = this.startDate
        val today = LocalDate.now()
        val step: (LocalDate) -> LocalDate = { d ->
            when (this.billingCycle.name) {
                "WEEKLY" -> d.plusWeeks(1)
                "YEARLY" -> d.plusYears(1)
                else -> d.plusMonths(1)
            }
        }
        
        while (true) {
            val nextDateStr = nextDate.toString()
            if (!list.contains(nextDateStr) && !nextDate.isBefore(today)) {
                break
            }
            if (list.contains(nextDateStr)) {
                nextDate = step(nextDate)
            } else {
                break
            }
        }

        return this.copy(
            confirmedPaymentDates = list.joinToString(","),
            nextPaymentDate = nextDate,
            updatedAt = today
        )
    }

    fun skipOverdueAndConfirmCurrent(today: LocalDate = LocalDate.now()): Subscription {
        val unconfirmed = getUnconfirmedPaymentDates(today)
        if (unconfirmed.isEmpty()) return this
        
        val currentPaymentDate = unconfirmed.last()
        
        val dateStr = currentPaymentDate.toString()
        val list = this.confirmedPaymentDates.split(",").filter { it.isNotBlank() }.toMutableList()
        if (!list.contains(dateStr)) {
            list.add(dateStr)
        }
        
        var nextDate = currentPaymentDate
        val step: (LocalDate) -> LocalDate = { d ->
            when (this.billingCycle.name) {
                "WEEKLY" -> d.plusWeeks(1)
                "YEARLY" -> d.plusYears(1)
                else -> d.plusMonths(1)
            }
        }
        nextDate = step(nextDate)
        
        return this.copy(
            confirmedPaymentDates = list.joinToString(","),
            nextPaymentDate = nextDate,
            updatedAt = today
        )
    }
}

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
    NEEDS_REVIEW,
    ENDED
}