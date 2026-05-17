package com.example.subrek.features.dashboard.domain.repository

import com.example.subrek.features.dashboard.domain.model.DashboardStats
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun getDashboardStats(): Flow<DashboardStats>
}
