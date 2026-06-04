package com.uzgram.messenger.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uzgram.messenger.domain.model.User
import com.uzgram.messenger.domain.repository.AuthRepository
import com.uzgram.messenger.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean = false,
    val isVerified: Boolean = false,
    val user: User? = null,
    val error: String? = null
)

sealed class AuthEvent {
    object LoginSuccess : AuthEvent()
    object LogoutSuccess : AuthEvent()
    data class Error(val message: String) : AuthEvent()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    init {
        authRepository.currentUser
            .onEach { user ->
                _authState.update {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = user != null,
                        user = user,
                        error = null
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.login(email, password)) {
                is Result.Success -> {
                    _authState.update {
                        it.copy(isLoading = false, isLoggedIn = true, user = result.data)
                    }
                    _events.emit(AuthEvent.LoginSuccess)
                }
                is Result.Error -> {
                    _authState.update { it.copy(isLoading = false, error = result.message) }
                    _events.emit(AuthEvent.Error(result.message))
                }
                else -> {}
            }
        }
    }

    fun register(
        email: String,
        password: String,
        displayName: String,
        username: String,
        bio: String? = null
    ) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.register(email, password, displayName, username, bio)) {
                is Result.Success -> {
                    _authState.update {
                        it.copy(isLoading = false, isLoggedIn = true, user = result.data)
                    }
                    _events.emit(AuthEvent.LoginSuccess)
                }
                is Result.Error -> {
                    _authState.update { it.copy(isLoading = false, error = result.message) }
                    _events.emit(AuthEvent.Error(result.message))
                }
                else -> {}
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true) }
            authRepository.logout()
            _authState.update { AuthState(isLoading = false, isLoggedIn = false) }
            _events.emit(AuthEvent.LogoutSuccess)
        }
    }

    fun verifyEmail(code: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.verifyEmail(code)) {
                is Result.Success -> {
                    _authState.update { it.copy(isLoading = false, isVerified = true) }
                }
                is Result.Error -> {
                    _authState.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> {}
            }
        }
    }

    fun resendVerificationCode(email: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.resendVerification(email)) {
                is Result.Error -> {
                    _authState.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> _authState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun clearError() {
        _authState.update { it.copy(error = null) }
    }
}
