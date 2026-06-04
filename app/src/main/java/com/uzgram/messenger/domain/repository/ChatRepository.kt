package com.uzgram.messenger.domain.repository

import com.uzgram.messenger.domain.model.Chat
import com.uzgram.messenger.domain.model.Message
import com.uzgram.messenger.utils.Result
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeChats(): Flow<List<Chat>>
    fun observeMessages(chatId: String): Flow<List<Message>>

    suspend fun getChats(cursor: String? = null): Result<List<Chat>>
    suspend fun getChatById(chatId: String): Result<Chat>
    suspend fun getOrCreatePrivateChat(userId: String): Result<Chat>
    suspend fun createGroupChat(name: String, memberIds: List<String>, description: String? = null): Result<Chat>
    suspend fun createChannel(name: String, description: String? = null, username: String? = null): Result<Chat>

    suspend fun getMessages(chatId: String, before: String? = null, limit: Int = 50): Result<List<Message>>
    suspend fun sendMessage(
        chatId: String,
        text: String? = null,
        type: String = "text",
        mediaUrl: String? = null,
        replyToId: String? = null
    ): Result<Message>
    suspend fun editMessage(messageId: String, text: String): Result<Message>
    suspend fun deleteMessage(messageId: String): Result<Unit>
    suspend fun reactToMessage(messageId: String, emoji: String): Result<Message>
    suspend fun markAsRead(messageId: String): Result<Unit>
    suspend fun searchMessages(query: String, chatId: String? = null): Result<List<Message>>
    suspend fun pinMessage(messageId: String): Result<Unit>
}
