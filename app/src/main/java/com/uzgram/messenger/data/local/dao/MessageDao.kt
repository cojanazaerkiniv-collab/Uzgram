package com.uzgram.messenger.data.local.dao

import androidx.room.*
import com.uzgram.messenger.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAtMs DESC LIMIT :limit")
    fun observeMessages(chatId: String, limit: Int = 100): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAtMs DESC LIMIT :limit OFFSET :offset")
    suspend fun getMessages(chatId: String, limit: Int = 50, offset: Int = 0): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getMessageById(id: String): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessage(id: String)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteAllMessagesForChat(chatId: String)

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND text LIKE '%' || :query || '%' ORDER BY createdAtMs DESC")
    suspend fun searchMessages(chatId: String, query: String): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE text LIKE '%' || :query || '%' ORDER BY createdAtMs DESC LIMIT 50")
    suspend fun searchAllMessages(query: String): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND isPinned = 1")
    fun observePinnedMessages(chatId: String): Flow<List<MessageEntity>>

    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: String)

    @Query("DELETE FROM messages WHERE chatId = :chatId AND createdAtMs < :beforeMs")
    suspend fun deleteOldMessages(chatId: String, beforeMs: Long)
}
