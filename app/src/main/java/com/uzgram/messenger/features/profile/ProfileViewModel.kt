package com.uzgram.messenger.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uzgram.messenger.domain.model.User
import com.uzgram.messenger.domain.repository.AuthRepository
import com.uzgram.messenger.domain.repository.StoryRepository
import com.uzgram.messenger.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val postsCount: Int = 0,
    val storiesCount: Int = 0,
    val contactsCount: Int = 0,
    val isLoading: Boolean = false,
    val showSignOutConfirm: Boolean = false,
    val showAvatarOptions: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val storyRepository: StoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        observeCurrentUser()
        loadStoriesCount()
        loadContactsCount()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            userRepository.getCurrentUser()
                .catch { e -> _state.update { it.copy(error = e.message) } }
                .collect { user -> _state.update { it.copy(user = user) } }
        }
    }

    private fun loadStoriesCount() {
        viewModelScope.launch {
            storyRepository.getMyStories()
                .catch { }
                .collect { stories ->
                    _state.update { it.copy(storiesCount = stories.size) }
                }
        }
    }

    private fun loadContactsCount() {
        viewModelScope.launch {
            userRepository.getContacts()
                .catch { }
                .collect { contacts ->
                    _state.update { it.copy(contactsCount = contacts.size) }
                }
        }
    }

    fun showAvatarOptions() {
        _state.update { it.copy(showAvatarOptions = true) }
    }

    fun dismissAvatarOptions() {
        _state.update { it.copy(showAvatarOptions = false) }
    }

    fun showSignOutConfirm() {
        _state.update { it.copy(showSignOutConfirm = true) }
    }

    fun dismissSignOut() {
        _state.update { it.copy(showSignOutConfirm = false) }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, showSignOutConfirm = false) }
            }
        }
    }

    fun updateAvatar(uri: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                userRepository.updateProfile(
                    displayName = null,
                    bio = null,
                    username = null,
                    avatarUri = uri
                )
                _state.update { it.copy(isLoading = false, showAvatarOptions = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
