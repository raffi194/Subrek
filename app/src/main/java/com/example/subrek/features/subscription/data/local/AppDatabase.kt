package com.example.subrek.features.subscription.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [SubscriptionEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class) // --- TAMBAHKAN INI ---
abstract class AppDatabase : RoomDatabase() {
    abstract fun subscriptionDao(): SubscriptionDao
}