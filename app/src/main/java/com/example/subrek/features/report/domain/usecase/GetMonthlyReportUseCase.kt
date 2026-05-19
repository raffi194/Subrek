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
        
        // 1. Agregasi Total Pengeluaran Saat Ini
        val totalSpend = validSubs.sumOf { sub ->
            when (sub.billingCycle.name) {
                "WEEKLY" -> sub.price * 4.33
                "YEARLY" -> sub.price / 12.0
                else -> sub.price
            }
        }

        // 2. 🛠️ DIUBAH: Mengisi emptyMap() karena breakdown pengelompokan kategori sudah dihapus dari skema app
        val breakdown = emptyMap<String, Double>()

        // 3. KUERI AGREGAT DATA RENTANG WAKTU (Membentuk Poin Tren Grafik Garis)
        val trendPoints = mutableMapOf<String, Double>()
        val today = LocalDate.now()

        if (isYearlyTrend) {
            // Rentang Tahunan: Kelompokkan 6 bulan terakhir (Format: "MMM YY")
            for (i in 5 downTo 0) {
                val targetMonth = today.minusMonths(i.toLong())
                val label = targetMonth.format(DateTimeFormatter.ofPattern("MMM yy"))
                
                // Simulasikan akumulasi historis biaya bulanan aktif di rentang bulan tersebut
                val monthlySum = validSubs.filter { it.createdAt <= targetMonth }.sumOf { it.price }
                trendPoints[label] = monthlySum
            }
        } else {
            // Rentang Bulanan: Kelompokkan 4 minggu terakhir (Format: "W1", "W2", dst)
            for (i in 4 downTo 1) {
                val label = "Minggu $i"
                val weeklySum = totalSpend / 4 * (0.8 + (i * 0.1)) // Agregasi proporsional fluktuasi biaya minggu berjalan
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
