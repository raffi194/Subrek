package com.example.subrek.features.report.domain.model

data class SubscriptionReport(
    val month: String,
    val totalExpense: Double,
    val categoryBreakdown: Map<String, Double>
)
