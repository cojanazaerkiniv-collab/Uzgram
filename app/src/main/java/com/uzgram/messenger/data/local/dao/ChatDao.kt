package com.uzgram.messenger.data.local.dao

import androidx.room.*
import com.uzgram.messenger.data.local.entity.ChatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Query("SELECT * FROM chats ORDER BY isPinned DESC, updatedAtMs DESC")
    fun observeChats(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE id = :chatId")
    suspend fun getChatById(chatId: String): ChatEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChats(chats: List<ChatEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    @Update
    suspend fun updateChat(chat: ChatEntity)

    @Query("DELETE FROM chats WHERE id = :chatId")
    suspend fun deleteChat(chatId: String)

    @Query("UPDATE chats SET unreadCount = 0 WHERE id = :chatId")
    suspend fun clearUnreadCount(chatId: String)

    @Query("UPDATE chats SET isPinned = :isPinned WHERE id = :chatId")
    suspend fun setPinned(chatId: String, isPinned: Boolean)

    @Query("UPDATE chats SET isMuted = :isMuted WHERE id = :chatId")
    suspend fun setMuted(chatId: String, isMuted: Boolean)

    @Query("SELECT * FROM chats WHERE unreadCount > 0")
    fun observeUnreadChats(): Flow<List<ChatEntity>>

    @Query("SELECT SUM(unreadCount) FROM chats")
    fun observeTotalUnread(): Flow<Int?>

    @Query("UPDATE chats SET lastMessageJson = :messageJson, updatedAtMs = :updatedAtMs WHERE id = :chatId")
    suspend fun updateLastMessage(chatId: String, messageJson: String, updatedAtMs: Long)

    @Query("UPDATE chats SET unreadCount = unreadCount + 1 WHERE id = :chatId")
    suspend fun incrementUnreadCount(chatId: String)
}
