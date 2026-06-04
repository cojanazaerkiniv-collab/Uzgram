package com.uzgram.messenger.domain.model

import java.time.Instant

enum class StoryMediaType { IMAGE, VIDEO, TEXT }
enum class StoryPrivacy { EVERYONE, CONTACTS, CLOSE_FRIENDS, EXCLUDE }

data class Story(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorUsername: String,
    val authorAvatarUrl: String?,
    val mediaUrl: String?,
    val mediaType: StoryMediaType,
    val textContent: String?,
    val backgroundColor: Long?,
    val gradientColors: List<Long>,
    val privacy: StoryPrivacy,
    val viewCount: Int,
    val reactionCount: Int,
    val viewers: List<StoryViewer>,
    val reactions: List<StoryReaction>,
    val expiresAt: Instant,
    val createdAt: Instant,
    val isSeen: Boolean,
    val isOwn: Boolean,
    val hasLink: Boolean,
    val linkUrl: String?,
    val linkTitle: String?,
    val stickers: List<StoryStickerPlacement>
)

data class StoryGroup(
    val userId: String,
    val userName: String,
    val userAvatarUrl: String?,
    val stories: List<Story>,
    val unreadCount: Int,
    val isOwn: Boolean
)

data class StoryViewer(
    val userId: String,
    val userName: String,
    val avatarUrl: String?,
    val viewedAt: Instant,
    val reaction: String?
)

data class StoryReaction(
    val userId: String,
    val emoji: String,
    val createdAt: Instant
)

data class StoryStickerPlacement(
    val stickerId: String,
    val stickerUrl: String,
    val x: Float,
    val y: Float,
    val rotation: Float,
    val scale: Float
)

data class StoryHighlight(
    val id: String,
    val title: String,
    val coverUrl: String?,
    val stories: List<Story>,
    val createdAt: Instant
)

data class CreateStoryRequest(
    val mediaUri: String?,
    val mediaType: StoryMediaType,
    val textContent: String?,
    val backgroundColor: Long?,
    val gradientColors: List<Long>,
    val privacy: StoryPrivacy,
    val excludedUserIds: List<String>,
    val closeFriendUserIds: List<String>,
    val linkUrl: String?,
    val linkTitle: String?,
    val stickers: List<StoryStickerPlacement>
)
