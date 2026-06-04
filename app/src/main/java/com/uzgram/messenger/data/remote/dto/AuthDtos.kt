package com.uzgram.messenger.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AuthResponseDto(
    @SerializedName("access_token")  val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("token_type")    val tokenType: String = "Bearer",
    @SerializedName("expires_in")    val expiresIn: Long = 3600,
    @SerializedName("user")          val user: UserDto
)

data class LoginRequest(
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("email")        val email: String,
    @SerializedName("password")     val password: String,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("username")     val username: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null
)

data class RefreshTokenRequest(
    @SerializedName("refresh_token") val refreshToken: String
)
