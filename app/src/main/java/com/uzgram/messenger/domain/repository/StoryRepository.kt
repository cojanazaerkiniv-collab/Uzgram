package com.uzgram.messenger.domain.repository

import com.uzgram.messenger.domain.model.CreateStoryRequest
import com.uzgram.messenger.domain.model.Story
import com.uzgram.messenger.domain.model.StoryGroup
import com.uzgram.messenger.domain.model.StoryHighlight
import kotlinx.coroutines.flow.Flow

interface StoryRepository {
    fun getStoryFeed(): Flow<List<StoryGroup>>
    fun getStoriesForUser(userId: String): Flow<List<Story>>
    fun getMyStories(): Flow<List<Story>>
    suspend fun createStory(request: CreateStoryRequest): Story
    suspend fun deleteStory(storyId: String)
    suspend fun viewStory(storyId: String)
    suspend fun reactToStory(storyId: String, emoji: String)
    suspend fun replyToStory(storyId: String, text: String)
    fun getHighlights(userId: String): Flow<List<StoryHighlight>>
    suspend fun createHighlight(title: String, coverStoryId: String?, storyIds: List<String>): StoryHighlight
    suspend fun addToHighlight(highlightId: String, storyId: String)
    suspend fun deleteHighlight(highlightId: String)
}
