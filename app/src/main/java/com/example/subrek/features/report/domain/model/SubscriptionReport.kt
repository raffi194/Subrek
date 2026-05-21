package com.example.subrek.features.report.domain.model


data class SubscriptionReport(
    val totalSpend: Double,
    val categoryBreakdown: Map<String, Double>,
    val trendPoints: Map<String, Double>
)
