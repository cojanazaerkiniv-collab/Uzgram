package com.uzgram.messenger.domain.model

data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val senderUsername: String,
    val senderAvatarUrl: String? = null,
    val type: MessageType = MessageType.TEXT,
    val text: String? = null,
    val mediaUrl: String? = null,
    val mediaType: String? = null,
    val fileName: String? = null,
    val fileSize: Long? = null,
    val replyToId: String? = null,
    val replyToText: String? = null,
    val reactions: List<MessageReaction> = emptyList(),
    val isEdited: Boolean = false,
    val isPinned: Boolean = false,
    val readBy: List<String> = emptyList(),
    val status: MessageStatus = MessageStatus.SENT,
    val createdAt: String = "",
    val updatedAt: String = ""
)

data class MessageReaction(
    val emoji: String,
    val count: Int,
    val userIds: List<String>
)

enum class MessageType {
    TEXT, IMAGE, FILE, VOICE, VIDEO, SYSTEM, STICKER, POLL, LOCATION
}

enum class MessageStatus {
    PENDING, SENT, DELIVERED, READ, FAILED
}
