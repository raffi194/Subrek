package com.example.subrek.features.dashboard.data

import com.example.subrek.features.dashboard.domain.model.DashboardStats
import com.example.subrek.features.dashboard.domain.repository.DashboardRepository
import com.example.subrek.features.subscription.data.local.SubscriptionDao
import com.example.subrek.features.subscription.data.mapper.toDomain
import com.example.subrek.features.dashboard.domain.usecase.GetDashboardStatsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor(
    private val subscriptionDao: SubscriptionDao,
    private val getDashboardStatsUseCase: GetDashboardStatsUseCase
) : DashboardRepository {
    
    override fun getDashboardStats(): Flow<DashboardStats> {
        return subscriptionDao.getAllSubscriptions().map { entities ->
            val domainList = entities.map { it.toDomain() }
            getDashboardStatsUseCase(domainList)
        }
    }
}
