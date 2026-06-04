package com.uzgram.messenger.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uzgram.messenger.domain.model.StoryGroup
import com.uzgram.messenger.domain.model.User
import com.uzgram.messenger.domain.repository.StoryRepository
import com.uzgram.messenger.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val storyGroups: List<StoryGroup> = emptyList(),
    val onlineContacts: List<User> = emptyList(),
    val currentUser: User? = null,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val storyRepository: StoryRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        observeCurrentUser()
        loadStories()
        loadOnlineContacts()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            userRepository.getCurrentUser()
                .catch { e -> _state.update { it.copy(error = e.message) } }
                .collect { user -> _state.update { it.copy(currentUser = user) } }
        }
    }

    private fun loadStories() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            storyRepository.getStoryFeed()
                .catch { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
                .collect { groups ->
                    _state.update { it.copy(storyGroups = groups, isLoading = false) }
                }
        }
    }

    private fun loadOnlineContacts() {
        viewModelScope.launch {
            userRepository.getOnlineContacts()
                .catch { }
                .collect { contacts ->
                    _state.update { it.copy(onlineContacts = contacts) }
                }
        }
    }

    fun refresh() {
        loadStories()
        loadOnlineContacts()
    }
}
