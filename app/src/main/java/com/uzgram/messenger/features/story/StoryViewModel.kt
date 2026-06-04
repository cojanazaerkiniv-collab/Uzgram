package com.uzgram.messenger.features.story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uzgram.messenger.domain.model.CreateStoryRequest
import com.uzgram.messenger.domain.model.Story
import com.uzgram.messenger.domain.model.StoryGroup
import com.uzgram.messenger.domain.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StoryUiState(
    val isLoading: Boolean = false,
    val storyGroups: List<StoryGroup> = emptyList(),
    val currentGroupStories: List<Story> = emptyList(),
    val error: String? = null
)

data class StoryEditorState(
    val isPublishing: Boolean = false,
    val publishError: String? = null
)

@HiltViewModel
class StoryViewModel @Inject constructor(
    private val storyRepository: StoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StoryUiState())
    val state: StateFlow<StoryUiState> = _state.asStateFlow()

    private val _editorState = MutableStateFlow(StoryEditorState())
    val editorState: StateFlow<StoryEditorState> = _editorState.asStateFlow()

    fun loadStoriesForUser(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                storyRepository.getStoriesForUser(userId)
                    .collect { stories ->
                        _state.update { it.copy(currentGroupStories = stories, isLoading = false) }
                    }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun publishStory(request: CreateStoryRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _editorState.update { it.copy(isPublishing = true, publishError = null) }
            try {
                storyRepository.createStory(request)
                _editorState.update { it.copy(isPublishing = false) }
                onSuccess()
            } catch (e: Exception) {
                _editorState.update { it.copy(isPublishing = false, publishError = e.message) }
            }
        }
    }

    fun deleteStory(storyId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                storyRepository.deleteStory(storyId)
                onSuccess()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun replyToStory(storyId: String, text: String) {
        viewModelScope.launch {
            try { storyRepository.replyToStory(storyId, text) } catch (_: Exception) {}
        }
    }

    fun reactToStory(storyId: String, emoji: String) {
        viewModelScope.launch {
            try { storyRepository.reactToStory(storyId, emoji) } catch (_: Exception) {}
        }
    }

    fun showViewers(storyId: String) {
        // Navigate to viewers bottom sheet
    }
}
