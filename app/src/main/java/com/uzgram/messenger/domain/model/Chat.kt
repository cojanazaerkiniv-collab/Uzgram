package com.uzgram.messenger.domain.model

data class Chat(
    val id: String,
    val type: ChatType,
    val name: String? = null,
    val description: String? = null,
    val avatarUrl: String? = null,
    val username: String? = null,
    val memberCount: Int = 0,
    val lastMessage: Message? = null,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val otherUser: User? = null,
    val createdAt: String = "",
    val updatedAt: String = ""
) {
    val displayName: String
        get() = when (type) {
            ChatType.PRIVATE -> otherUser?.displayName ?: name ?: "Unknown"
            else -> name ?: "Unnamed"
        }

    val displayAvatar: String?
        get() = when (type) {
            ChatType.PRIVATE -> otherUser?.avatarUrl ?: avatarUrl
            else -> avatarUrl
        }

    val isOnline: Boolean
        get() = type == ChatType.PRIVATE && otherUser?.isOnline == true
}

enum class ChatType {
    PRIVATE, GROUP, CHANNEL
}
