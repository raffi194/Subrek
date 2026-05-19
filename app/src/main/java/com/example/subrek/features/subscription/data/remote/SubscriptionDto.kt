package com.example.subrek.features.subscription.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object (DTO) untuk kebutuhan payload API Supabase Postgrest.
 */
@Serializable
data class SubscriptionDto(
    @SerialName("id")
    val id: String,
    @SerialName("user_id")
    val userId: String, // Diperlukan untuk validasi RLS multi-tenant di cloud
    @SerialName("name")
    val name: String,
    @SerialName("price")
    val price: Double,
    @SerialName("currency")
    val currency: String,
    @SerialName("billing_cycle")
    val billingCycle: String,
    // 🛍️ Dihapus karena di PostgreSQL hanya menggunakan next_payment_date
    @SerialName("next_payment_date")
    val nextPaymentDate: String,
    @SerialName("category")
    val category: String,
    @SerialName("payment_method")
    val paymentMethod: String,
    @SerialName("is_trial")
    val isTrial: Boolean,
    @SerialName("is_ghost_subscription")
    val isGhostSubscription: Boolean,
    @SerialName("status")
    val status: String,
    @SerialName("unconfirmed_cycles_count")
    val unconfirmedCyclesCount: Int = 0,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("icon_url")
    val iconUrl: String? = null //  Ditambahkan agar sinkron dengan kolom icon_url di DB
)
