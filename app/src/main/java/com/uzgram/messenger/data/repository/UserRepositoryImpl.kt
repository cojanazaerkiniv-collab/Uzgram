package com.uzgram.messenger.data.repository

import com.uzgram.messenger.data.local.UzGramDatabase
import com.uzgram.messenger.data.local.preferences.UserPreferences
import com.uzgram.messenger.data.remote.ApiService
import com.uzgram.messenger.data.remote.dto.UserDto
import com.uzgram.messenger.domain.model.User
import com.uzgram.messenger.domain.repository.UserRepository
import com.uzgram.messenger.utils.Result
import com.uzgram.messenger.utils.safeApiCall
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val db: UzGramDatabase,
    private val prefs: UserPreferences
) : UserRepository {

    // Emits current user from prefs (cached) and refreshes from server
    override fun getCurrentUser(): Flow<User> = flow {
        // Emit cached first
        prefs.currentUser.first()?.let { emit(it.toDomain()) }
        // Refresh from server
        when (val r = safeApiCall { api.getMe() }) {
            is Result.Success -> {
                prefs.saveCurrentUser(r.data)
                emit(r.data.toDomain())
            }
            is Result.Error -> throw Exception(r.message)
            else -> {}
        }
    }

    override fun getOnlineContacts(): Flow<List<User>> = flow {
        when (val r = safeApiCall { api.getOnlineContacts() }) {
            is Result.Success -> emit(r.data.map { it.toDomain() })
            is Result.Error   -> throw Exception(r.message)
            else -> {}
        }
    }

    override fun searchUsers(query: String): Flow<List<User>> = flow {
        when (val r = safeApiCall { api.searchUsers(query) }) {
            is Result.Success -> emit(r.data.map { it.toDomain() })
            is Result.Error   -> throw Exception(r.message)
            else -> {}
        }
    }

    override suspend fun getUserById(userId: String): User {
        return when (val r = safeApiCall { api.getUserById(userId) }) {
            is Result.Success -> r.data.toDomain()
            is Result.Error   -> throw Exception(r.message)
            else              -> throw Exception("Unknown error")
        }
    }

    override suspend fun getUserByUsername(username: String): User {
        return when (val r = safeApiCall { api.getUserByUsername(username) }) {
            is Result.Success -> r.data.toDomain()
            is Result.Error   -> throw Exception(r.message)
            else              -> throw Exception("Unknown error")
        }
    }

    override suspend fun updateProfile(
        displayName: String?,
        bio: String?,
        username: String?,
        avatarUri: String?
    ): User {
        val body = buildMap<String, Any?> {
            displayName?.let { put("display_name", it) }
            bio?.let { put("bio", it) }
            username?.let { put("username", it) }
            avatarUri?.let { put("avatar_uri", it) }
        }
        return when (val r = safeApiCall { api.updateProfile(body) }) {
            is Result.Success -> {
                prefs.saveCurrentUser(r.data)
                r.data.toDomain()
            }
            is Result.Error -> throw Exception(r.message)
            else            -> throw Exception("Unknown error")
        }
    }

    override suspend fun blockUser(userId: String) {
        when (val r = safeApiCall { api.blockUser(userId) }) {
            is Result.Error -> throw Exception(r.message)
            else -> {}
        }
    }

    override suspend fun unblockUser(userId: String) {
        when (val r = safeApiCall { api.unblockUser(userId) }) {
            is Result.Error -> throw Exception(r.message)
            else -> {}
        }
    }

    override suspend fun getBlockedUsers(): List<User> {
        return when (val r = safeApiCall { api.getBlockedUsers() }) {
            is Result.Success -> r.data.map { it.toDomain() }
            is Result.Error   -> throw Exception(r.message)
            else              -> emptyList()
        }
    }

    override suspend fun addContact(userId: String) {
        safeApiCall { api.addContact(userId) }
    }

    override suspend fun removeContact(userId: String) {
        safeApiCall { api.removeContact(userId) }
    }

    override fun getContacts(): Flow<List<User>> = flow {
        when (val r = safeApiCall { api.getContacts() }) {
            is Result.Success -> emit(r.data.map { it.toDomain() })
            is Result.Error   -> throw Exception(r.message)
            else -> {}
        }
    }

    override suspend fun syncContacts(phoneNumbers: List<String>): List<User> {
        return when (val r = safeApiCall { api.syncContacts(mapOf("phone_numbers" to phoneNumbers)) }) {
            is Result.Success -> r.data.map { it.toDomain() }
            is Result.Error   -> throw Exception(r.message)
            else              -> emptyList()
        }
    }

    override suspend fun reportUser(userId: String, reason: String) {
        safeApiCall { api.reportUser(userId, mapOf("reason" to reason)) }
    }
}

private fun UserDto.toDomain(): User = User(
    id = id,
    email = email,
    displayName = displayName,
    username = username,
    bio = bio,
    avatarUrl = avatarUrl,
    isOnline = isOnline,
    lastSeen = lastSeen,
    isVerified = isVerified ?: false,
    isPremium = isPremium ?: false,
    phoneNumber = phoneNumber
)
