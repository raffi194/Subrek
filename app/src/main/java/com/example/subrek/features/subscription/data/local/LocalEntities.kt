package com.example.subrek.features.subscription.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_categories")
data class LocalCategoryEntity(
    @PrimaryKey val id: String,
    val name: String
)

@Entity(tableName = "local_apps")
data class LocalAppEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconUrl: String?,
    val categoryName: String
)
