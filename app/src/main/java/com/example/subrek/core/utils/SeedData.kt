package com.example.subrek.core.utils

import com.example.subrek.features.subscription.data.local.LocalAppEntity
import com.example.subrek.features.subscription.data.local.LocalCategoryEntity
import com.example.subrek.features.subscription.data.local.SubscriptionEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object SeedData {

    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE
    private val today = LocalDate.now()

    // =========================================================
    // 1. KATEGORI DEFAULT
    // =========================================================
    val defaultCategories = listOf(
        LocalCategoryEntity(id = "cat_hiburan",      name = "Hiburan"),
        LocalCategoryEntity(id = "cat_productivity", name = "Productivity"),
        LocalCategoryEntity(id = "cat_utilitas",     name = "Utilitas"),
        LocalCategoryEntity(id = "cat_kesehatan",    name = "Kesehatan"),
        LocalCategoryEntity(id = "cat_finansial",    name = "Finansial"),
        LocalCategoryEntity(id = "cat_cineman",      name = "Cineman"),
        LocalCategoryEntity(id = "cat_music",        name = "Music"),
        LocalCategoryEntity(id = "cat_social",       name = "Social Network"),
        LocalCategoryEntity(id = "cat_popular",      name = "Popular"),
    )

    // =========================================================
    // 2. KATALOG APP DEFAULT
    // =========================================================
    val defaultApps = listOf(
        LocalAppEntity(
            id = "app_netflix",
            name = "Netflix",
            iconUrl = "https://i.pinimg.com/280x280_RS/0d/12/5b/0d125bef05d84ce60294293ad8ad6d26.jpg"
        )
    )

    // =========================================================
    // 3. CONTOH DATA LANGGANAN DEFAULT (Demo/Onboarding)
    // =========================================================
    val demoSubscriptions = listOf(
        SubscriptionEntity(
            id = "demo_netflix",
            name = "Netflix",
            price = 54000.0,
            currency = "IDR",
            billingCycle = "MONTHLY",
            startDate = today.minusMonths(3).format(fmt),
            nextPaymentDate = today.plusDays(7).format(fmt),
            paymentMethod = "Kartu Kredit",
            isTrial = false,
            isGhostSubscription = false,
            status = "ACTIVE",
            unconfirmedCyclesCount = 0,
            createdAt = today.minusMonths(3).format(fmt),
            iconUrl = "https://i.pinimg.com/280x280_RS/0d/12/5b/0d125bef05d84ce60294293ad8ad6d26.jpg"
        )
    )
}