package com.example.subrek.features.report.data

import com.example.subrek.features.report.domain.model.SubscriptionReport
import com.example.subrek.features.report.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor() : ReportRepository {
    override fun getMonthlyReport(month: String): Flow<SubscriptionReport> {
        // Implementation for calculating reports from Room or Remote data
        return flowOf(
            SubscriptionReport(
                month = month,
                totalExpense = 0.0,
                categoryBreakdown = emptyMap()
            )
        )
    }
}
