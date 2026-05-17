package com.example.subrek.features.dashboard.domain.model

data class DashboardStats(
    val totalMonthlySpend: Double,
    val upcomingBillsCount: Int,
    val mostExpensiveSubscription: String?,
    val currency: String = "IDR"
)
