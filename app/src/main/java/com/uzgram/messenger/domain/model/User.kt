package com.uzgram.messenger.domain.model

data class User(
    val id: String,
    val email: String?,
    val displayName: String,
    val username: String?,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val phoneNumber: String? = null,
    val isVerified: Boolean = false,
    val isPremium: Boolean = false,
    val isAdmin: Boolean = false,
    val isBanned: Boolean = false,
    val isOnline: Boolean = false,
    val lastSeen: String? = null,
    val createdAt: String = ""
)
