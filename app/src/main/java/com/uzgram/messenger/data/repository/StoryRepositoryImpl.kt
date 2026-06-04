package com.uzgram.messenger.data.repository

import com.uzgram.messenger.data.remote.ApiService
import com.uzgram.messenger.data.remote.dto.*
import com.uzgram.messenger.domain.model.*
import com.uzgram.messenger.domain.repository.StoryRepository
import com.uzgram.messenger.utils.Result
import com.uzgram.messenger.utils.safeApiCall
import kotlinx.coroutines.flow.*
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoryRepositoryImpl @Inject constructor(
    private val api: ApiService
) : StoryRepository {

    override fun getStoryFeed(): Flow<List<StoryGroup>> = flow {
        val result = safeApiCall { api.getStoryFeed() }
        when (result) {
            is Result.Success -> emit(result.data.map { it.toDomain() })
            is Result.Error -> throw Exception(result.message)
            else -> {}
        }
    }

    override fun getStoriesForUser(userId: String): Flow<List<Story>> = flow {
        val result = safeApiCall { api.getUserStories(userId) }
        when (result) {
            is Result.Success -> emit(result.data.map { it.toDomain() })
            is Result.Error -> throw Exception(result.message)
            else -> {}
        }
    }

    override fun getMyStories(): Flow<List<Story>> = flow {
        val result = safeApiCall { api.getMyStories() }
        when (result) {
            is Result.Success -> emit(result.data.map { it.toDomain() })
            is Result.Error -> throw Exception(result.message)
            else -> {}
        }
    }

    override suspend fun createStory(request: CreateStoryRequest): Story {
        val dto = CreateStoryDto(
            mediaUri = request.mediaUri,
            mediaType = request.mediaType.name.lowercase(),
            textContent = request.textContent,
            backgroundColor = request.backgroundColor?.toString(),
            gradientColors = request.gradientColors.map { it.toString() },
            privacy = request.privacy.name.lowercase(),
            excludedUserIds = request.excludedUserIds,
            closeFriendUserIds = request.closeFriendUserIds,
            linkUrl = request.linkUrl,
            linkTitle = request.linkTitle
        )
        return when (val result = safeApiCall { api.createStory(dto) }) {
            is Result.Success -> result.data.toDomain()
            is Result.Error -> throw Exception(result.message)
            else -> throw Exception("Unknown error")
        }
    }

    override suspend fun deleteStory(storyId: String) {
        when (val result = safeApiCall { api.deleteStory(storyId) }) {
            is Result.Error -> throw Exception(result.message)
            else -> {}
        }
    }

    override suspend fun viewStory(storyId: String) {
        safeApiCall { api.viewStory(storyId) }
    }

    override suspend fun reactToStory(storyId: String, emoji: String) {
        safeApiCall { api.reactToStory(storyId, mapOf("emoji" to emoji)) }
    }

    override suspend fun replyToStory(storyId: String, text: String) {
        safeApiCall { api.replyToStory(storyId, mapOf("text" to text)) }
    }

    override fun getHighlights(userId: String): Flow<List<StoryHighlight>> = flow {
        val result = safeApiCall { api.getHighlights(userId) }
        when (result) {
            is Result.Success -> emit(result.data.map { it.toDomain() })
            is Result.Error -> throw Exception(result.message)
            else -> {}
        }
    }

    override suspend fun createHighlight(title: String, coverStoryId: String?, storyIds: List<String>): StoryHighlight {
        return when (val result = safeApiCall {
            api.createHighlight(mapOf("title" to title, "coverStoryId" to coverStoryId, "storyIds" to storyIds))
        }) {
            is Result.Success -> result.data.toDomain()
            is Result.Error -> throw Exception(result.message)
            else -> throw Exception("Unknown error")
        }
    }

    override suspend fun addToHighlight(highlightId: String, storyId: String) {
        safeApiCall { api.addToHighlight(highlightId, mapOf("storyId" to storyId)) }
    }

    override suspend fun deleteHighlight(highlightId: String) {
        safeApiCall { api.deleteHighlight(highlightId) }
    }
}

// --- Mappers ---
private fun StoryDto.toDomain(): Story = Story(
    id = id,
    authorId = authorId,
    authorName = authorName,
    authorUsername = authorUsername,
    authorAvatarUrl = authorAvatarUrl,
    mediaUrl = mediaUrl,
    mediaType = when (mediaType) {
        "video" -> StoryMediaType.VIDEO
        "text" -> StoryMediaType.TEXT
        else -> StoryMediaType.IMAGE
    },
    textContent = textContent,
    backgroundColor = backgroundColor?.toLongOrNull(),
    gradientColors = gradientColors.mapNotNull { it.toLongOrNull() },
    privacy = when (privacy) {
        "contacts" -> StoryPrivacy.CONTACTS
        "close_friends" -> StoryPrivacy.CLOSE_FRIENDS
        "exclude" -> StoryPrivacy.EXCLUDE
        else -> StoryPrivacy.EVERYONE
    },
    viewCount = viewCount,
    reactionCount = reactionCount,
    viewers = viewers.map { it.toDomain() },
    reactions = reactions.map { it.toDomain() },
    expiresAt = Instant.parse(expiresAt),
    createdAt = Instant.parse(createdAt),
    isSeen = isSeen,
    isOwn = isOwn,
    hasLink = linkUrl != null,
    linkUrl = linkUrl,
    linkTitle = linkTitle,
    stickers = emptyList()
)

private fun StoryGroupDto.toDomain(): StoryGroup = StoryGroup(
    userId = userId,
    userName = userName,
    userAvatarUrl = userAvatarUrl,
    stories = stories.map { it.toDomain() },
    unreadCount = unreadCount,
    isOwn = isOwn
)

private fun StoryViewerDto.toDomain(): StoryViewer = StoryViewer(
    userId = userId,
    userName = userName,
    avatarUrl = avatarUrl,
    viewedAt = Instant.parse(viewedAt),
    reaction = reaction
)

private fun StoryReactionDto.toDomain(): StoryReaction = StoryReaction(
    userId = userId,
    emoji = emoji,
    createdAt = Instant.parse(createdAt)
)

private fun HighlightDto.toDomain(): StoryHighlight = StoryHighlight(
    id = id,
    title = title,
    coverUrl = coverUrl,
    stories = stories.map { it.toDomain() },
    createdAt = Instant.parse(createdAt)
)
