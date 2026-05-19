package com.example.subrek.features.auth.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val email: String,
    val fullName: String? = null,
    val avatarUrl: String? = null
)
