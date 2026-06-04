package com.uzgram.messenger.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("access_token")  val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("user")          val user: UserDto
)

data class SuccessResponse(
    val success: Boolean = true,
    val message: String? = null
)

data class UserDto(
    @SerializedName("id")           val id: String,
    @SerializedName("username")     val username: String?,
    @SerializedName("email")        val email: String?,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("bio")          val bio: String? = null,
    @SerializedName("avatar_url")   val avatarUrl: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("is_verified")  val isVerified: Boolean? = false,
    @SerializedName("is_premium")   val isPremium: Boolean? = false,
    @SerializedName("is_admin")     val isAdmin: Boolean? = false,
    @SerializedName("is_banned")    val isBanned: Boolean? = false,
    @SerializedName("is_online")    val isOnline: Boolean = false,
    @SerializedName("last_seen")    val lastSeen: String? = null,
    @SerializedName("created_at")   val createdAt: String = ""
)

data class ChatDto(
    @SerializedName("id")            val id: String,
    @SerializedName("type")          val type: String,
    @SerializedName("name")          val name: String? = null,
    @SerializedName("description")   val description: String? = null,
    @SerializedName("avatar_url")    val avatarUrl: String? = null,
    @SerializedName("username")      val username: String? = null,
    @SerializedName("member_count")  val memberCount: Int = 0,
    @SerializedName("last_message")  val lastMessage: MessageDto? = null,
    @SerializedName("unread_count")  val unreadCount: Int = 0,
    @SerializedName("is_pinned")     val isPinned: Boolean = false,
    @SerializedName("is_muted")      val isMuted: Boolean = false,
    @SerializedName("other_user")    val otherUser: UserDto? = null,
    @SerializedName("created_at")    val createdAt: String = "",
    @SerializedName("updated_at")    val updatedAt: String = ""
)

data class ChatListResponse(
    @SerializedName("chats")       val chats: List<ChatDto>,
    @SerializedName("next_cursor") val nextCursor: String? = null
)

data class ChatMemberDto(
    @SerializedName("user_id")      val userId: String,
    @SerializedName("role")         val role: String,
    @SerializedName("custom_title") val customTitle: String? = null,
    @SerializedName("joined_at")    val joinedAt: String,
    @SerializedName("user")         val user: UserDto
)

data class ChatsSummaryDto(
    @SerializedName("total_chats")      val totalChats: Int,
    @SerializedName("total_unread")     val totalUnread: Int,
    @SerializedName("active_chats")     val activeChats: Int,
    @SerializedName("pinned_chats")     val pinnedChats: Int,
    @SerializedName("online_contacts")  val onlineContacts: Int
)

data class MessageReactionDto(
    @SerializedName("emoji")    val emoji: String,
    @SerializedName("count")    val count: Int,
    @SerializedName("user_ids") val userIds: List<String>
)

data class MessageDto(
    @SerializedName("id")              val id: String,
    @SerializedName("chat_id")         val chatId: String,
    @SerializedName("sender_id")       val senderId: String,
    @SerializedName("sender_name")     val senderName: String,
    @SerializedName("sender_username") val senderUsername: String,
    @SerializedName("sender_avatar_url") val senderAvatarUrl: String? = null,
    @SerializedName("type")            val type: String = "text",
    @SerializedName("text")            val text: String? = null,
    @SerializedName("media_url")       val mediaUrl: String? = null,
    @SerializedName("media_type")      val mediaType: String? = null,
    @SerializedName("file_name")       val fileName: String? = null,
    @SerializedName("file_size")       val fileSize: Long? = null,
    @SerializedName("reply_to_id")     val replyToId: String? = null,
    @SerializedName("reply_to_text")   val replyToText: String? = null,
    @SerializedName("reactions")       val reactions: List<MessageReactionDto> = emptyList(),
    @SerializedName("is_edited")       val isEdited: Boolean = false,
    @SerializedName("is_pinned")       val isPinned: Boolean = false,
    @SerializedName("read_by")         val readBy: List<String> = emptyList(),
    @SerializedName("created_at")      val createdAt: String = "",
    @SerializedName("updated_at")      val updatedAt: String = ""
)

data class MessageListResponse(
    @SerializedName("messages")    val messages: List<MessageDto>,
    @SerializedName("has_more")    val hasMore: Boolean = false,
    @SerializedName("next_cursor") val nextCursor: String? = null
)

data class AdminStatsDto(
    @SerializedName("total_users")          val totalUsers: Int,
    @SerializedName("active_users_today")   val activeUsersToday: Int,
    @SerializedName("active_users_week")    val activeUsersWeek: Int,
    @SerializedName("total_messages")       val totalMessages: Int,
    @SerializedName("messages_today")       val messagesToday: Int,
    @SerializedName("total_chats")          val totalChats: Int,
    @SerializedName("active_chats")         val activeChats: Int = 0,
    @SerializedName("banned_users")         val bannedUsers: Int,
    @SerializedName("new_users_today")      val newUsersToday: Int,
    @SerializedName("new_users_week")       val newUsersWeek: Int,
    @SerializedName("total_stories")        val totalStories: Int = 0,
    @SerializedName("total_mini_apps")      val totalMiniApps: Int = 0,
    @SerializedName("premium_users")        val premiumUsers: Int = 0,
    @SerializedName("storage_gb")          val storageGb: Float = 0f,
    @SerializedName("registrations_by_day") val registrationsByDay: List<DayCountDto> = emptyList(),
    @SerializedName("messages_by_day")      val messagesByDay: List<DayCountDto> = emptyList()
)

data class DayCountDto(
    @SerializedName("date")  val date: String,
    @SerializedName("count") val count: Int
)

data class AdminUsersResponse(
    @SerializedName("users") val users: List<UserDto>,
    @SerializedName("total") val total: Int
)

data class AdminChatsResponse(
    @SerializedName("chats") val chats: List<ChatDto>,
    @SerializedName("total") val total: Int
)
