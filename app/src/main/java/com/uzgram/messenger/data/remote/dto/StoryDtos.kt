package com.uzgram.messenger.data.remote.dto

import com.google.gson.annotations.SerializedName

data class StoryDto(
    @SerializedName("id") val id: String,
    @SerializedName("author_id") val authorId: String,
    @SerializedName("author_name") val authorName: String,
    @SerializedName("author_username") val authorUsername: String,
    @SerializedName("author_avatar_url") val authorAvatarUrl: String?,
    @SerializedName("media_url") val mediaUrl: String?,
    @SerializedName("media_type") val mediaType: String,
    @SerializedName("text_content") val textContent: String?,
    @SerializedName("background_color") val backgroundColor: String?,
    @SerializedName("gradient_colors") val gradientColors: List<String>,
    @SerializedName("privacy") val privacy: String,
    @SerializedName("view_count") val viewCount: Int,
    @SerializedName("reaction_count") val reactionCount: Int,
    @SerializedName("viewers") val viewers: List<StoryViewerDto>,
    @SerializedName("reactions") val reactions: List<StoryReactionDto>,
    @SerializedName("expires_at") val expiresAt: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("is_seen") val isSeen: Boolean,
    @SerializedName("is_own") val isOwn: Boolean,
    @SerializedName("link_url") val linkUrl: String?,
    @SerializedName("link_title") val linkTitle: String?
)

data class StoryGroupDto(
    @SerializedName("user_id") val userId: String,
    @SerializedName("user_name") val userName: String,
    @SerializedName("user_avatar_url") val userAvatarUrl: String?,
    @SerializedName("stories") val stories: List<StoryDto>,
    @SerializedName("unread_count") val unreadCount: Int,
    @SerializedName("is_own") val isOwn: Boolean
)

data class StoryViewerDto(
    @SerializedName("user_id") val userId: String,
    @SerializedName("user_name") val userName: String,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("viewed_at") val viewedAt: String,
    @SerializedName("reaction") val reaction: String?
)

data class StoryReactionDto(
    @SerializedName("user_id") val userId: String,
    @SerializedName("emoji") val emoji: String,
    @SerializedName("created_at") val createdAt: String
)

data class HighlightDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("cover_url") val coverUrl: String?,
    @SerializedName("stories") val stories: List<StoryDto>,
    @SerializedName("created_at") val createdAt: String
)

data class CreateStoryDto(
    @SerializedName("media_uri") val mediaUri: String?,
    @SerializedName("media_type") val mediaType: String,
    @SerializedName("text_content") val textContent: String?,
    @SerializedName("background_color") val backgroundColor: String?,
    @SerializedName("gradient_colors") val gradientColors: List<String>,
    @SerializedName("privacy") val privacy: String,
    @SerializedName("excluded_user_ids") val excludedUserIds: List<String>,
    @SerializedName("close_friend_user_ids") val closeFriendUserIds: List<String>,
    @SerializedName("link_url") val linkUrl: String?,
    @SerializedName("link_title") val linkTitle: String?
)
