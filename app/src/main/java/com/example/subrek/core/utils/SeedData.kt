package com.example.subrek.core.utils

import com.example.subrek.features.subscription.data.local.LocalAppEntity
import com.example.subrek.features.subscription.data.local.LocalCategoryEntity
import com.example.subrek.features.subscription.data.local.SubscriptionEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object SeedData {

    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE
    private val today = LocalDate.now()
    val defaultApps = listOf(
        LocalAppEntity(
            id = "app_netflix",
            name = "Netflix",
            iconUrl = "https://img.icons8.com/color/512/netflix-desktop-app.png"
        ),
        LocalAppEntity(
            id = "app_spotify",
            name = "Spotify",
            iconUrl = "https://img.icons8.com/color/512/spotify--v1.png"
        ),
        LocalAppEntity(
            id = "app_youtube",
            name = "YouTube",
            iconUrl = "https://img.icons8.com/color/512/youtube-play.png"
        ),
        LocalAppEntity(
            id = "app_prime",
            name = "Prime Video",
            iconUrl = "https://img.icons8.com/color/512/amazon-prime-video.png"
        ),
        LocalAppEntity(
            id = "app_notion",
            name = "Notion",
            iconUrl = "https://img.icons8.com/color/512/notion.png"
        ),
        LocalAppEntity(
            id = "app_canva",
            name = "Canva",
            iconUrl = "https://img.icons8.com/color/512/canva.png"
        ),
        LocalAppEntity(
            id = "app_zoom",
            name = "Zoom",
            iconUrl = "https://img.icons8.com/color/512/zoom.png"
        ),
        LocalAppEntity(
            id = "app_claude",
            name = "Claude",
            iconUrl = "https://cdn-1.webcatalog.io/catalog/claude/claude-icon-filled-256.webp?v=1776040508943"
        ),
        LocalAppEntity(
            id = "app_chatgpt",
            name = "ChatGPT",
            iconUrl = "https://img.icons8.com/fluency/512/chatgpt.png"
        ),
        LocalAppEntity(
            id = "app_disney",
            name = "Disney+",
            iconUrl = "https://img.icons8.com/color/512/disney-plus.png"
        ),
        LocalAppEntity(
            id = "app_hbogo",
            name = "HBO Go",
            iconUrl = "https://img.icons8.com/color/512/hbo.png"
        )
    )
}