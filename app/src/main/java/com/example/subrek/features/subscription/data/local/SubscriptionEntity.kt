package com.example.subrek.features.subscription.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity yang merepresentasikan tabel "subscriptions" di SQLite lokal.
 * Semua tipe data kompleks seperti Date dan Enum dikonversi menjadi String/VARCHAR.
 */
@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "price")
    val price: Double,
    
    @ColumnInfo(name = "currency")
    val currency: String,
    
    @ColumnInfo(name = "billing_cycle")
    val billingCycle: String, 
    
    @ColumnInfo(name = "start_date")
    val startDate: String, 
    
    @ColumnInfo(name = "next_payment_date")
    val nextPaymentDate: String, 
    
    @ColumnInfo(name = "payment_method")
    val paymentMethod: String,
    
    @ColumnInfo(name = "is_trial")
    val isTrial: Boolean,
    
    @ColumnInfo(name = "is_ghost_subscription")
    val isGhostSubscription: Boolean = false,
    
    @ColumnInfo(name = "status")
    val status: String, 

    @ColumnInfo(name = "unconfirmed_cycles_count")
    val unconfirmedCyclesCount: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: String, 

    @ColumnInfo(name = "is_dirty")
    val isDirty: Boolean = false, 

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(), 

    @ColumnInfo(name = "icon_url")
    val iconUrl: String? = null
)
