package com.example.subrek.features.report.domain.usecase

import com.example.subrek.features.report.domain.model.SubscriptionReport
import com.example.subrek.features.subscription.domain.model.Subscription
import com.example.subrek.features.subscription.domain.model.SubscriptionStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class GetMonthlyReportUseCase @Inject constructor() {

    operator fun invoke(subscriptions: List<Subscription>, isYearlyTrend: Boolean): SubscriptionReport {
        val validSubs = subscriptions.filter { it.status != SubscriptionStatus.PAUSED }
        

        val totalSpend = validSubs.sumOf { sub ->
            when (sub.billingCycle.name) {
                "YEARLY" -> sub.price / 12.0
                else -> sub.price
            }
        }

        val breakdown = emptyMap<String, Double>()

        val trendPoints = mutableMapOf<String, Double>()
        val today = LocalDate.now()

        if (isYearlyTrend) {
            for (i in 5 downTo 0) {
                val targetMonth = today.minusMonths(i.toLong())
                val label = targetMonth.format(DateTimeFormatter.ofPattern("MMM yy"))

                val monthlySum = validSubs.filter { 
                    it.createdAt <= targetMonth 
                }.sumOf { sub ->
                    when (sub.billingCycle) {
                        com.example.subrek.features.subscription.domain.model.BillingCycle.YEARLY -> sub.price / 12.0
                        else -> sub.price
                    }
                }
                trendPoints[label] = monthlySum
            }
        } else {
            for (i in 4 downTo 1) {
                val label = "Minggu $i"
                val weeklySum = totalSpend / 4 * (0.8 + (i * 0.05))
                trendPoints[label] = weeklySum
            }
        }

        return SubscriptionReport(
            totalSpend = totalSpend,
            categoryBreakdown = breakdown,
            trendPoints = trendPoints
        )
    }
}
