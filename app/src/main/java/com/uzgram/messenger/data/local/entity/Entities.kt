package com.uzgram.messenger.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val email: String,
    val displayName: String,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val isVerified: Boolean = false,
    val isPremium: Boolean = false,
    val isAdmin: Boolean = false,
    val isBanned: Boolean = false,
    val isOnline: Boolean = false,
    val lastSeen: String? = null,
    val createdAt: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chats", indices = [Index("updatedAt")])
data class ChatEntity(
    @PrimaryKey val id: String,
    val type: String,
    val name: String? = null,
    val description: String? = null,
    val avatarUrl: String? = null,
    val username: String? = null,
    val memberCount: Int = 0,
    val lastMessageJson: String? = null,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val otherUserId: String? = null,
    val createdAt: String = "",
    val updatedAt: String = "",
    val updatedAtMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "messages",
    indices = [Index("chatId"), Index("createdAt"), Index("senderId")],
    foreignKeys = [
        ForeignKey(
            entity = ChatEntity::class,
            parentColumns = ["id"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val senderUsername: String,
    val senderAvatarUrl: String? = null,
    val type: String = "text",
    val text: String? = null,
    val mediaUrl: String? = null,
    val mediaType: String? = null,
    val fileName: String? = null,
    val fileSize: Long? = null,
    val replyToId: String? = null,
    val replyToText: String? = null,
    val reactionsJson: String = "[]",
    val isEdited: Boolean = false,
    val isPinned: Boolean = false,
    val readByJson: String = "[]",
    val status: String = "sent",
    val createdAt: String = "",
    val updatedAt: String = "",
    val createdAtMs: Long = System.currentTimeMillis()
)
