package com.uzgram.messenger.data.local.dao

import androidx.room.*
import com.uzgram.messenger.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE id = :userId")
    fun observeUser(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String)

    @Query("SELECT * FROM users WHERE displayName LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%' LIMIT 20")
    suspend fun searchUsers(query: String): List<UserEntity>

    @Query("UPDATE users SET isOnline = :isOnline, lastSeen = :lastSeen WHERE id = :userId")
    suspend fun updateOnlineStatus(userId: String, isOnline: Boolean, lastSeen: String?)
}
