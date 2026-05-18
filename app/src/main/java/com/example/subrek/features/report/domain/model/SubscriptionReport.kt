package com.example.subrek.features.report.domain.model

/**
 * Model data untuk laporan statistik langganan.
 */
data class SubscriptionReport(
    val totalSpend: Double,
    val categoryBreakdown: Map<String, Double>,
    val trendPoints: Map<String, Double> // Poin koordinat untuk grafik garis (Label vs Nilai)
)
