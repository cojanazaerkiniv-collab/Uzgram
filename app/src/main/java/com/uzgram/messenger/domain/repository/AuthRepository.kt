package com.uzgram.messenger.domain.repository

import com.uzgram.messenger.domain.model.User
import com.uzgram.messenger.utils.Result
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    val accessToken: Flow<String?>

    suspend fun register(
        email: String,
        password: String,
        displayName: String,
        username: String,
        bio: String? = null
    ): Result<User>

    suspend fun login(email: String, password: String): Result<User>

    suspend fun logout(): Result<Unit>

    suspend fun refreshToken(): Result<String>

    suspend fun updateProfile(
        displayName: String? = null,
        username: String? = null,
        bio: String? = null,
        avatarUrl: String? = null
    ): Result<User>

    suspend fun getMe(): Result<User>

    suspend fun verifyEmail(code: String): Result<Unit>

    suspend fun resendVerification(email: String): Result<Unit>

    suspend fun forgotPassword(email: String): Result<Unit>

    suspend fun resetPassword(token: String, newPassword: String): Result<Unit>

    fun isLoggedIn(): Boolean
}
