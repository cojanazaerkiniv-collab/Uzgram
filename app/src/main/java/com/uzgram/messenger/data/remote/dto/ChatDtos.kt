package com.uzgram.messenger.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ChatDto(
    @SerializedName("id")               val id: String,
    @SerializedName("type")             val type: String,
    @SerializedName("name")             val name: String? = null,
    @SerializedName("avatar_url")       val avatarUrl: String? = null,
    @SerializedName("description")      val description: String? = null,
    @SerializedName("members")          val members: List<UserDto> = emptyList(),
    @SerializedName("last_message")     val lastMessage: MessageDto? = null,
    @SerializedName("unread_count")     val unreadCount: Int = 0,
    @SerializedName("is_pinned")        val isPinned: Boolean = false,
    @SerializedName("is_archived")      val isArchived: Boolean = false,
    @SerializedName("is_muted")         val isMuted: Boolean = false,
    @SerializedName("mute_until")       val muteUntil: String? = null,
    @SerializedName("created_at")       val createdAt: String,
    @SerializedName("updated_at")       val updatedAt: String,
    @SerializedName("member_count")     val memberCount: Int = 2,
    @SerializedName("is_group")         val isGroup: Boolean = false,
    @SerializedName("is_channel")       val isChannel: Boolean = false,
    @SerializedName("admin_ids")        val adminIds: List<String> = emptyList(),
    @SerializedName("invite_link")      val inviteLink: String? = null,
    @SerializedName("peer")             val peer: UserDto? = null
)

data class MessageDto(
    @SerializedName("id")              val id: String,
    @SerializedName("chat_id")         val chatId: String,
    @SerializedName("sender_id")       val senderId: String,
    @SerializedName("sender_name")     val senderName: String? = null,
    @SerializedName("sender_avatar")   val senderAvatar: String? = null,
    @SerializedName("type")            val type: String = "text",
    @SerializedName("text")            val text: String? = null,
    @SerializedName("media_url")       val mediaUrl: String? = null,
    @SerializedName("media_type")      val mediaType: String? = null,
    @SerializedName("media_size")      val mediaSize: Long? = null,
    @SerializedName("file_name")       val fileName: String? = null,
    @SerializedName("reply_to_id")     val replyToId: String? = null,
    @SerializedName("reply_to")        val replyTo: MessageDto? = null,
    @SerializedName("forward_from_id") val forwardFromId: String? = null,
    @SerializedName("is_edited")       val isEdited: Boolean = false,
    @SerializedName("is_deleted")      val isDeleted: Boolean = false,
    @SerializedName("is_read")         val isRead: Boolean = false,
    @SerializedName("reactions")       val reactions: List<MessageReactionDto> = emptyList(),
    @SerializedName("read_by")         val readBy: List<String> = emptyList(),
    @SerializedName("created_at")      val createdAt: String,
    @SerializedName("updated_at")      val updatedAt: String? = null,
    @SerializedName("ttl_seconds")     val ttlSeconds: Int? = null,
    @SerializedName("location_lat")    val locationLat: Double? = null,
    @SerializedName("location_lng")    val locationLng: Double? = null,
    @SerializedName("poll")            val poll: PollDto? = null,
    @SerializedName("sticker_id")      val stickerId: String? = null,
    @SerializedName("sticker_url")     val stickerUrl: String? = null
)

data class MessageReactionDto(
    @SerializedName("emoji")    val emoji: String,
    @SerializedName("count")    val count: Int,
    @SerializedName("user_ids") val userIds: List<String>
)

data class PollDto(
    @SerializedName("question")     val question: String,
    @SerializedName("options")      val options: List<PollOptionDto>,
    @SerializedName("is_anonymous") val isAnonymous: Boolean = true,
    @SerializedName("is_multiple")  val isMultiple: Boolean = false,
    @SerializedName("total_votes")  val totalVotes: Int = 0,
    @SerializedName("voted_option") val votedOption: Int? = null
)

data class PollOptionDto(
    @SerializedName("id")         val id: Int,
    @SerializedName("text")       val text: String,
    @SerializedName("vote_count") val voteCount: Int = 0
)
