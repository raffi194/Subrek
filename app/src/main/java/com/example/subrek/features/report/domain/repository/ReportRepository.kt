package com.example.subrek.features.report.domain.repository

import com.example.subrek.features.report.domain.model.SubscriptionReport
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    fun getMonthlyReport(month: String): Flow<SubscriptionReport>
}
