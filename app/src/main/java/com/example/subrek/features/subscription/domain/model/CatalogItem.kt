package com.example.subrek.features.subscription.domain.model

data class CatalogItem(
    val id: String,
    val name: String,
    val iconUrl: String?,
    val isCustom: Boolean = false
)
