package com.example.subrek.features.report.domain.usecase

import com.example.subrek.features.report.domain.model.SubscriptionReport
import com.example.subrek.features.report.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMonthlyReportUseCase @Inject constructor(
    private val repository: ReportRepository
) {
    operator fun invoke(month: String): Flow<SubscriptionReport> {
        return repository.getMonthlyReport(month)
    }
}
