package com.uzgram.messenger.data.repository

import com.uzgram.messenger.data.local.dao.UserDao
import com.uzgram.messenger.data.local.entity.UserEntity
import com.uzgram.messenger.data.local.prefs.SessionPrefs
import com.uzgram.messenger.data.remote.api.ApiService
import com.uzgram.messenger.data.remote.dto.*
import com.uzgram.messenger.data.remote.websocket.SocketManager
import com.uzgram.messenger.domain.model.User
import com.uzgram.messenger.domain.repository.AuthRepository
import com.uzgram.messenger.utils.Result
import com.uzgram.messenger.utils.safeApiCall
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val sessionPrefs: SessionPrefs,
    private val userDao: UserDao,
    private val socketManager: SocketManager
) : AuthRepository {

    override val currentUser: Flow<User?> = combine(
        sessionPrefs.userId,
        sessionPrefs.accessToken
    ) { userId, token ->
        if (userId != null && token != null) {
            userDao.getUserById(userId)?.toDomain()
        } else null
    }

    override val accessToken: Flow<String?> = sessionPrefs.accessToken

    override suspend fun register(
        email: String,
        password: String,
        displayName: String,
        username: String,
        bio: String?
    ): Result<User> = safeApiCall {
        val response = api.register(RegisterRequest(email, password, displayName, username, bio))
        handleAuthResponse(response)
        response.user.toDomain()
    }

    override suspend fun login(email: String, password: String): Result<User> = safeApiCall {
        val response = api.login(LoginRequest(email, password))
        handleAuthResponse(response)
        response.user.toDomain()
    }

    override suspend fun logout(): Result<Unit> = safeApiCall {
        try { api.logout() } catch (_: Exception) {}
        sessionPrefs.clearSession()
        socketManager.disconnect()
    }

    override suspend fun refreshToken(): Result<String> = safeApiCall {
        val token = sessionPrefs.refreshToken.first() ?: throw Exception("No refresh token")
        val response = api.refreshToken(RefreshTokenRequest(token))
        handleAuthResponse(response)
        response.accessToken
    }

    override suspend fun updateProfile(
        displayName: String?,
        username: String?,
        bio: String?,
        avatarUrl: String?
    ): Result<User> = safeApiCall {
        val body = buildMap<String, Any?> {
            displayName?.let { put("displayName", it) }
            username?.let { put("username", it) }
            bio?.let { put("bio", it) }
            avatarUrl?.let { put("avatarUrl", it) }
        }
        val dto = api.updateProfile(body)
        userDao.insertUser(dto.toEntity())
        dto.toDomain()
    }

    override suspend fun getMe(): Result<User> = safeApiCall {
        val dto = api.getMe()
        userDao.insertUser(dto.toEntity())
        dto.toDomain()
    }

    override suspend fun verifyEmail(code: String): Result<Unit> = safeApiCall {
        api.verifyEmail(mapOf("code" to code))
        Unit
    }

    override suspend fun resendVerification(email: String): Result<Unit> = safeApiCall {
        api.resendVerification(mapOf("email" to email))
        Unit
    }

    override suspend fun forgotPassword(email: String): Result<Unit> = safeApiCall {
        api.forgotPassword(ForgotPasswordRequest(email))
        Unit
    }

    override suspend fun resetPassword(token: String, newPassword: String): Result<Unit> = safeApiCall {
        api.resetPassword(ResetPasswordRequest(token, newPassword))
        Unit
    }

    override fun isLoggedIn(): Boolean = runCatching {
        kotlinx.coroutines.runBlocking { sessionPrefs.accessToken.first() != null }
    }.getOrDefault(false)

    private suspend fun handleAuthResponse(response: AuthResponse) {
        sessionPrefs.saveSession(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            userId = response.user.id,
            username = response.user.username,
            displayName = response.user.displayName,
            avatarUrl = response.user.avatarUrl,
            isAdmin = response.user.isAdmin
        )
        userDao.insertUser(response.user.toEntity())
        socketManager.connect(response.accessToken)
    }
}

fun UserDto.toDomain() = User(
    id = id,
    username = username,
    email = email,
    displayName = displayName,
    bio = bio,
    avatarUrl = avatarUrl,
    isVerified = isVerified,
    isPremium = isPremium,
    isAdmin = isAdmin,
    isBanned = isBanned,
    isOnline = isOnline,
    lastSeen = lastSeen,
    createdAt = createdAt
)

fun UserDto.toEntity() = UserEntity(
    id = id,
    username = username,
    email = email,
    displayName = displayName,
    bio = bio,
    avatarUrl = avatarUrl,
    isVerified = isVerified,
    isPremium = isPremium,
    isAdmin = isAdmin,
    isBanned = isBanned,
    isOnline = isOnline,
    lastSeen = lastSeen,
    createdAt = createdAt
)

fun UserEntity.toDomain() = User(
    id = id,
    username = username,
    email = email,
    displayName = displayName,
    bio = bio,
    avatarUrl = avatarUrl,
    isVerified = isVerified,
    isPremium = isPremium,
    isAdmin = isAdmin,
    isBanned = isBanned,
    isOnline = isOnline,
    lastSeen = lastSeen,
    createdAt = createdAt
)
