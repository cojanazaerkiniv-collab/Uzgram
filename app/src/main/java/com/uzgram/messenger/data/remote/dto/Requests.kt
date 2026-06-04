package com.uzgram.messenger.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val email: String,
    val password: String,
    @SerializedName("displayName") val displayName: String,
    val username: String,
    val bio: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)

data class UpdateProfileRequest(
    val displayName: String? = null,
    val username: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null
)

data class AddContactRequest(
    val userId: String
)

data class CreateChatRequest(
    val type: String,
    val name: String,
    val description: String? = null,
    val memberIds: List<String> = emptyList(),
    val username: String? = null
)

data class GetOrCreateChatRequest(
    val userId: String
)

data class UpdateChatRequest(
    val name: String? = null,
    val description: String? = null,
    val avatarUrl: String? = null
)

data class AddMembersRequest(
    val userIds: List<String>
)

data class SendMessageRequest(
    val type: String = "text",
    val text: String? = null,
    val mediaUrl: String? = null,
    val mediaType: String? = null,
    val fileName: String? = null,
    val fileSize: Long? = null,
    val replyToId: String? = null
)

data class EditMessageRequest(
    val text: String
)

data class ReactRequest(
    val emoji: String
)

data class SearchMessagesRequest(
    val q: String,
    val chatId: String? = null,
    val limit: Int = 20
)

data class BanUserRequest(
    val reason: String,
    val durationDays: Int? = null
)
