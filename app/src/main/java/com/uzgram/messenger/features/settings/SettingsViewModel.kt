package com.uzgram.messenger.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uzgram.messenger.domain.model.User
import com.uzgram.messenger.domain.repository.AuthRepository
import com.uzgram.messenger.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val user: User? = null,
    val showSignOutConfirm: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        observeCurrentUser()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            userRepository.getCurrentUser()
                .catch { e -> _state.update { it.copy(error = e.message) } }
                .collect { user -> _state.update { it.copy(user = user) } }
        }
    }

    fun showSignOut() {
        _state.update { it.copy(showSignOutConfirm = true) }
    }

    fun dismissSignOut() {
        _state.update { it.copy(showSignOutConfirm = false) }
    }

    fun confirmSignOut() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, showSignOutConfirm = false) }
            try {
                authRepository.signOut()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
