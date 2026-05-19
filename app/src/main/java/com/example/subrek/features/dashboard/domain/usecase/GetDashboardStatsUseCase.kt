package com.example.subrek.features.dashboard.domain.usecase

import com.example.subrek.features.dashboard.domain.model.DashboardStats
import com.example.subrek.features.subscription.domain.model.Subscription
import com.example.subrek.features.subscription.domain.model.SubscriptionStatus
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class GetDashboardStatsUseCase @Inject constructor() {
    
    operator fun invoke(subscriptions: List<Subscription>): DashboardStats {
        val activeSubs = subscriptions.filter { 
            it.status == SubscriptionStatus.ACTIVE || it.status == SubscriptionStatus.TRIAL 
        }
        
        val totalMonthlySpend = activeSubs.sumOf { sub ->
            when (sub.billingCycle.name) {
                "YEARLY" -> sub.price / 12.0
                else -> sub.price
            }
        }

        val today = LocalDate.now()
        val upcomingBillsCount = activeSubs.count { sub ->
            val daysToPayment = ChronoUnit.DAYS.between(today, sub.nextPaymentDate)
            daysToPayment in 0..7
        }

        val mostExpensive = activeSubs.maxByOrNull { it.price }?.name ?: "-"

        return DashboardStats(
            totalMonthlySpend = totalMonthlySpend,
            upcomingBillsCount = upcomingBillsCount,
            mostExpensiveSubscription = mostExpensive
        )
    }
}
