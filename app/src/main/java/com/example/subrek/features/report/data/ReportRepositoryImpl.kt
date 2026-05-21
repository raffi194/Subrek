package com.example.subrek.features.report.data

import com.example.subrek.features.report.domain.model.SubscriptionReport
import com.example.subrek.features.report.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor() : ReportRepository {
    override fun getMonthlyReport(month: String): Flow<SubscriptionReport> {
        return flowOf(
            SubscriptionReport(
                totalSpend = 0.0,
                categoryBreakdown = emptyMap(),
                trendPoints = emptyMap()
            )
        )
    }
}
