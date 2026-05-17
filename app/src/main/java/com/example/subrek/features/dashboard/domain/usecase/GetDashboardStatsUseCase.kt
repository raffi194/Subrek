package com.example.subrek.features.dashboard.domain.usecase

import com.example.subrek.features.dashboard.domain.model.DashboardStats
import com.example.subrek.features.dashboard.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDashboardStatsUseCase @Inject constructor(
    private val repository: DashboardRepository
) {
    operator fun invoke(): Flow<DashboardStats> {
        return repository.getDashboardStats()
    }
}
