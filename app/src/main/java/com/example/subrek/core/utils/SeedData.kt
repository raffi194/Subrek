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
            iconUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/08/Netflix_2015_logo.svg/200px-Netflix_2015_logo.svg.png",
            categoryName = "Cineman"
        ),
        LocalAppEntity(
            id = "app_spotify",
            name = "Spotify",
            iconUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/19/Spotify_logo_without_text.svg/200px-Spotify_logo_without_text.svg.png",
            categoryName = "Music"
        ),
        LocalAppEntity(
            id = "app_youtube",
            name = "YouTube Premium",
            iconUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/09/YouTube_full-color_icon_%282017%29.svg/200px-YouTube_full-color_icon_%282017%29.svg.png",
            categoryName = "Popular"
        ),
        LocalAppEntity(
            id = "app_twitter",
            name = "Twitter Blue",
            iconUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6f/Logo_of_Twitter.svg/200px-Logo_of_Twitter.svg.png",
            categoryName = "Social Network"
        ),
        LocalAppEntity(
            id = "app_notion",
            name = "Notion",
            iconUrl = "https://upload.wikimedia.org/wikipedia/commons/4/45/Notion_app_logo.png",
            categoryName = "Productivity"
        ),
        LocalAppEntity(
            id = "app_canva",
            name = "Canva Pro",
            iconUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/b/bb/Canva_Logo.svg/200px-Canva_Logo.svg.png",
            categoryName = "Productivity"
        ),
        LocalAppEntity(
            id = "app_disney",
            name = "Disney+ Hotstar",
            iconUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3e/Disney%2B_logo.svg/200px-Disney%2B_logo.svg.png",
            categoryName = "Cineman"
        ),
        LocalAppEntity(
            id = "app_icloud",
            name = "iCloud+",
            iconUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1c/ICloud_logo.svg/200px-ICloud_logo.svg.png",
            categoryName = "Utilitas"
        ),
        LocalAppEntity(
            id = "app_googledrive",
            name = "Google One",
            iconUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/da/Google_Drive_logo.png/200px-Google_Drive_logo.png",
            categoryName = "Utilitas"
        ),
        LocalAppEntity(
            id = "app_vidio",
            name = "Vidio",
            iconUrl = "https://placeholder.co/100",
            categoryName = "Cineman"
        ),
        LocalAppEntity(
            id = "app_mola",
            name = "Mola TV",
            iconUrl = "https://placeholder.co/100",
            categoryName = "Cineman"
        ),
        LocalAppEntity(
            id = "app_joox",
            name = "JOOX Premium",
            iconUrl = "https://placeholder.co/100",
            categoryName = "Music"
        ),
    )

    // =========================================================
    // 3. CONTOH DATA LANGGANAN DEFAULT (Demo/Onboarding)
    // Akan disisipkan HANYA jika tabel subscriptions masih kosong
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
            isDirty = false
        ),
        SubscriptionEntity(
            id = "demo_spotify",
            name = "Spotify",
            price = 54990.0,
            currency = "IDR",
            billingCycle = "MONTHLY",
            startDate = today.minusMonths(5).format(fmt),
            nextPaymentDate = today.plusDays(14).format(fmt),
            paymentMethod = "E-Wallet",
            isTrial = false,
            isGhostSubscription = false,
            status = "ACTIVE",
            unconfirmedCyclesCount = 0,
            createdAt = today.minusMonths(5).format(fmt),
            isDirty = false
        ),
        SubscriptionEntity(
            id = "demo_youtube",
            name = "YouTube Premium",
            price = 59000.0,
            currency = "IDR",
            billingCycle = "MONTHLY",
            startDate = today.minusMonths(1).format(fmt),
            nextPaymentDate = today.plusDays(3).format(fmt),
            paymentMethod = "Kartu Kredit",
            isTrial = false,
            isGhostSubscription = false,
            status = "ACTIVE",
            unconfirmedCyclesCount = 0,
            createdAt = today.minusMonths(1).format(fmt),
            isDirty = false
        ),
        SubscriptionEntity(
            id = "demo_notion",
            name = "Notion",
            price = 160000.0,
            currency = "IDR",
            billingCycle = "YEARLY",
            startDate = today.minusMonths(2).format(fmt),
            nextPaymentDate = today.plusDays(30).format(fmt),
            paymentMethod = "Transfer Bank",
            isTrial = false,
            isGhostSubscription = false,
            status = "TRIAL",
            unconfirmedCyclesCount = 0,
            createdAt = today.minusMonths(2).format(fmt),
            isDirty = false
        ),
        SubscriptionEntity(
            id = "demo_old_app",
            name = "Mola TV",
            price = 39000.0,
            currency = "IDR",
            billingCycle = "MONTHLY",
            startDate = today.minusYears(1).format(fmt),
            nextPaymentDate = today.minusMonths(1).format(fmt),
            paymentMethod = "E-Wallet",
            isTrial = false,
            isGhostSubscription = true,
            status = "ENDED",
            unconfirmedCyclesCount = 3,
            createdAt = today.minusYears(1).format(fmt),
            isDirty = false
        ),
    )
}
