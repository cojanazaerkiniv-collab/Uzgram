package com.uzgram.messenger.data.repository

import com.google.gson.Gson
import com.uzgram.messenger.data.local.dao.ChatDao
import com.uzgram.messenger.data.local.dao.MessageDao
import com.uzgram.messenger.data.local.dao.UserDao
import com.uzgram.messenger.data.local.entity.ChatEntity
import com.uzgram.messenger.data.local.entity.MessageEntity
import com.uzgram.messenger.data.remote.api.ApiService
import com.uzgram.messenger.data.remote.dto.*
import com.uzgram.messenger.data.remote.websocket.SocketEvent
import com.uzgram.messenger.data.remote.websocket.SocketManager
import com.uzgram.messenger.domain.model.*
import com.uzgram.messenger.domain.repository.ChatRepository
import com.uzgram.messenger.utils.Result
import com.uzgram.messenger.utils.safeApiCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val userDao: UserDao,
    private val socketManager: SocketManager,
    private val gson: Gson
) : ChatRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        observeSocketForMessages()
    }

    override fun observeChats(): Flow<List<Chat>> =
        chatDao.observeChats().map { entities -> entities.map { it.toDomain(gson) } }

    override fun observeMessages(chatId: String): Flow<List<Message>> =
        messageDao.observeMessages(chatId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getChats(cursor: String?): Result<List<Chat>> = safeApiCall {
        val response = api.getChats(cursor = cursor)
        val entities = response.chats.map { it.toEntity(gson) }
        chatDao.insertChats(entities)
        entities.map { it.toDomain(gson) }
    }

    override suspend fun getChatById(chatId: String): Result<Chat> = safeApiCall {
        val dto = api.getChatById(chatId)
        chatDao.insertChat(dto.toEntity(gson))
        dto.toDomain(gson)
    }

    override suspend fun getOrCreatePrivateChat(userId: String): Result<Chat> = safeApiCall {
        val dto = api.getOrCreatePrivateChat(GetOrCreateChatRequest(userId))
        chatDao.insertChat(dto.toEntity(gson))
        dto.toDomain(gson)
    }

    override suspend fun createGroupChat(name: String, memberIds: List<String>, description: String?): Result<Chat> =
        safeApiCall {
            val dto = api.createChat(CreateChatRequest("group", name, description, memberIds))
            chatDao.insertChat(dto.toEntity(gson))
            dto.toDomain(gson)
        }

    override suspend fun createChannel(name: String, description: String?, username: String?): Result<Chat> =
        safeApiCall {
            val dto = api.createChat(CreateChatRequest("channel", name, description, username = username))
            chatDao.insertChat(dto.toEntity(gson))
            dto.toDomain(gson)
        }

    override suspend fun getMessages(chatId: String, before: String?, limit: Int): Result<List<Message>> =
        safeApiCall {
            val response = api.getMessages(chatId, limit, before)
            val entities = response.messages.map { it.toEntity() }
            messageDao.insertMessages(entities)
            entities.map { it.toDomain() }
        }

    override suspend fun sendMessage(
        chatId: String,
        text: String?,
        type: String,
        mediaUrl: String?,
        replyToId: String?
    ): Result<Message> = safeApiCall {
        val dto = api.sendMessage(chatId, SendMessageRequest(type, text, mediaUrl, replyToId = replyToId))
        messageDao.insertMessage(dto.toEntity())
        chatDao.updateLastMessage(chatId, gson.toJson(dto), System.currentTimeMillis())
        dto.toDomain()
    }

    override suspend fun editMessage(messageId: String, text: String): Result<Message> = safeApiCall {
        val dto = api.editMessage(messageId, EditMessageRequest(text))
        messageDao.insertMessage(dto.toEntity())
        dto.toDomain()
    }

    override suspend fun deleteMessage(messageId: String): Result<Unit> = safeApiCall {
        api.deleteMessage(messageId)
        messageDao.deleteMessage(messageId)
    }

    override suspend fun reactToMessage(messageId: String, emoji: String): Result<Message> = safeApiCall {
        val dto = api.reactToMessage(messageId, ReactRequest(emoji))
        messageDao.insertMessage(dto.toEntity())
        dto.toDomain()
    }

    override suspend fun markAsRead(messageId: String): Result<Unit> = safeApiCall {
        api.markMessageRead(messageId)
    }

    override suspend fun searchMessages(query: String, chatId: String?): Result<List<Message>> = safeApiCall {
        val response = api.searchMessages(SearchMessagesRequest(query, chatId))
        response.messages.map { it.toDomain() }
    }

    override suspend fun pinMessage(messageId: String): Result<Unit> = safeApiCall {
        val msg = messageDao.getMessageById(messageId)
        if (msg != null) {
            api.pinMessage(msg.chatId, messageId)
        }
    }

    private fun observeSocketForMessages() {
        scope.launch {
            socketManager.events.collect { event ->
                when (event) {
                    is SocketEvent.NewMessage -> {
                        messageDao.insertMessage(event.message.toEntity())
                        chatDao.incrementUnreadCount(event.message.chatId)
                    }
                    is SocketEvent.MessageDeleted -> {
                        messageDao.deleteMessage(event.messageId)
                    }
                    is SocketEvent.UserOnline -> {
                        userDao.updateOnlineStatus(event.userId, true, null)
                    }
                    is SocketEvent.UserOffline -> {
                        userDao.updateOnlineStatus(event.userId, false, event.lastSeen)
                    }
                    else -> {}
                }
            }
        }
    }
}

// ── Mappers ───────────────────────────────────────────────────────────────────

fun ChatDto.toEntity(gson: Gson) = ChatEntity(
    id = id,
    type = type,
    name = name,
    description = description,
    avatarUrl = avatarUrl,
    username = username,
    memberCount = memberCount,
    lastMessageJson = lastMessage?.let { gson.toJson(it) },
    unreadCount = unreadCount,
    isPinned = isPinned,
    isMuted = isMuted,
    otherUserId = otherUser?.id,
    createdAt = createdAt,
    updatedAt = updatedAt,
    updatedAtMs = System.currentTimeMillis()
)

fun ChatEntity.toDomain(gson: Gson): Chat {
    val lastMsg = lastMessageJson?.let {
        runCatching { gson.fromJson(it, MessageDto::class.java).toDomain() }.getOrNull()
    }
    return Chat(
        id = id,
        type = ChatType.valueOf(type.uppercase()),
        name = name,
        description = description,
        avatarUrl = avatarUrl,
        username = username,
        memberCount = memberCount,
        lastMessage = lastMsg,
        unreadCount = unreadCount,
        isPinned = isPinned,
        isMuted = isMuted,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun ChatDto.toDomain(gson: Gson) = toEntity(gson).toDomain(gson)

fun MessageDto.toEntity() = MessageEntity(
    id = id,
    chatId = chatId,
    senderId = senderId,
    senderName = senderName,
    senderUsername = senderUsername,
    senderAvatarUrl = senderAvatarUrl,
    type = type,
    text = text,
    mediaUrl = mediaUrl,
    mediaType = mediaType,
    fileName = fileName,
    fileSize = fileSize,
    replyToId = replyToId,
    replyToText = replyToText,
    reactionsJson = com.google.gson.Gson().toJson(reactions),
    isEdited = isEdited,
    isPinned = isPinned,
    readByJson = com.google.gson.Gson().toJson(readBy),
    status = "sent",
    createdAt = createdAt,
    updatedAt = updatedAt,
    createdAtMs = System.currentTimeMillis()
)

fun MessageEntity.toDomain(): Message {
    val gson = com.google.gson.Gson()
    val reactions = runCatching {
        gson.fromJson(reactionsJson, Array<com.uzgram.messenger.data.remote.dto.MessageReactionDto>::class.java)
            .map { MessageReaction(it.emoji, it.count, it.userIds) }
    }.getOrDefault(emptyList())
    val readBy = runCatching {
        gson.fromJson(readByJson, Array<String>::class.java).toList()
    }.getOrDefault(emptyList())

    return Message(
        id = id,
        chatId = chatId,
        senderId = senderId,
        senderName = senderName,
        senderUsername = senderUsername,
        senderAvatarUrl = senderAvatarUrl,
        type = runCatching { MessageType.valueOf(type.uppercase()) }.getOrDefault(MessageType.TEXT),
        text = text,
        mediaUrl = mediaUrl,
        mediaType = mediaType,
        fileName = fileName,
        fileSize = fileSize,
        replyToId = replyToId,
        replyToText = replyToText,
        reactions = reactions,
        isEdited = isEdited,
        isPinned = isPinned,
        readBy = readBy,
        status = runCatching { MessageStatus.valueOf(status.uppercase()) }.getOrDefault(MessageStatus.SENT),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun MessageDto.toDomain() = toEntity().toDomain()

fun Message.toEntity() = MessageEntity(
    id = id,
    chatId = chatId,
    senderId = senderId,
    senderName = senderName,
    senderUsername = senderUsername,
    senderAvatarUrl = senderAvatarUrl,
    type = type.name.lowercase(),
    text = text,
    mediaUrl = mediaUrl,
    mediaType = mediaType,
    fileName = fileName,
    fileSize = fileSize,
    replyToId = replyToId,
    replyToText = replyToText,
    reactionsJson = com.google.gson.Gson().toJson(
        reactions.map { com.uzgram.messenger.data.remote.dto.MessageReactionDto(it.emoji, it.count, it.userIds) }
    ),
    isEdited = isEdited,
    isPinned = isPinned,
    readByJson = com.google.gson.Gson().toJson(readBy),
    status = status.name.lowercase(),
    createdAt = createdAt,
    updatedAt = updatedAt,
    createdAtMs = System.currentTimeMillis()
)
