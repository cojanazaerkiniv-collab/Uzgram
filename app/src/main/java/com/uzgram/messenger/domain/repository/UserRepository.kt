package com.uzgram.messenger.domain.repository

import com.uzgram.messenger.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getCurrentUser(): Flow<User>
    fun getOnlineContacts(): Flow<List<User>>
    fun searchUsers(query: String): Flow<List<User>>
    suspend fun getUserById(userId: String): User
    suspend fun getUserByUsername(username: String): User
    suspend fun updateProfile(
        displayName: String?,
        bio: String?,
        username: String?,
        avatarUri: String?
    ): User
    suspend fun blockUser(userId: String)
    suspend fun unblockUser(userId: String)
    suspend fun getBlockedUsers(): List<User>
    suspend fun addContact(userId: String)
    suspend fun removeContact(userId: String)
    fun getContacts(): Flow<List<User>>
    suspend fun syncContacts(phoneNumbers: List<String>): List<User>
    suspend fun reportUser(userId: String, reason: String)
}
