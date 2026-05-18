package com.example.subrek.features.dashboard.domain.usecase

import com.example.subrek.features.dashboard.domain.model.DashboardStats
import com.example.subrek.features.subscription.domain.model.Subscription
import com.example.subrek.features.subscription.domain.model.SubscriptionStatus
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class GetDashboardStatsUseCase @Inject constructor() {
    
    // Menerima list data riil dari Room, lalu mengkalkulasi statistik akumulatif secara dinamis
    operator fun invoke(subscriptions: List<Subscription>): DashboardStats {
        val activeSubs = subscriptions.filter { it.status == SubscriptionStatus.ACTIVE || it.status == SubscriptionStatus.TRIAL }
        
        val totalMonthlySpend = activeSubs.sumOf { sub ->
            // Konversikan semua harga pengeluaran secara proporsional ke rentang bulanan murni
            when (sub.billingCycle.name) {
                "WEEKLY" -> sub.price * 4.33
                "YEARLY" -> sub.price / 12.0
                else -> sub.price // MONTHLY
            }
        }

        val today = LocalDate.now()
        val upcomingBillsCount = activeSubs.count { sub ->
            val daysToPayment = ChronoUnit.DAYS.between(today, sub.nextPaymentDate)
            daysToPayment in 0..7 // Jatuh tempo dalam waktu 7 hari ke depan
        }

        val mostExpensive = activeSubs.maxByOrNull { it.price }?.name ?: "-"

        return DashboardStats(
            totalMonthlySpend = totalMonthlySpend,
            upcomingBillsCount = upcomingBillsCount,
            mostExpensiveSubscription = mostExpensive
        )
    }
}
