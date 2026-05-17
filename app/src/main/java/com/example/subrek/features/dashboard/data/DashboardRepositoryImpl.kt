package com.example.subrek.features.dashboard.data

import com.example.subrek.features.dashboard.domain.model.DashboardStats
import com.example.subrek.features.dashboard.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor() : DashboardRepository {
    override fun getDashboardStats(): Flow<DashboardStats> {
        // Dummy data for now
        return flowOf(
            DashboardStats(
                totalMonthlySpend = 0.0,
                upcomingBillsCount = 0,
                mostExpensiveSubscription = null
            )
        )
    }
}
