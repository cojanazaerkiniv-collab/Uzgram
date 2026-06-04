package com.uzgram.messenger.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uzgram.messenger.data.local.preferences.UserPreferences
import com.uzgram.messenger.domain.model.Chat
import com.uzgram.messenger.domain.model.Message
import com.uzgram.messenger.domain.repository.ChatRepository
import com.uzgram.messenger.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class MessagingUiState(
    val chatName: String = "",
    val isGroupChat: Boolean = false,
    val isOnline: Boolean = false,
    val isTyping: Boolean = false,
    val lastSeen: String? = null,
    val currentUserId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MessagingViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessagingUiState())
    val uiState: StateFlow<MessagingUiState> = _uiState.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private var typingJob: Job? = null
    private var currentChatId: String = ""

    init {
        viewModelScope.launch {
            userPreferences.currentUser.first()?.let { user ->
                _uiState.update { it.copy(currentUserId = user.id) }
            }
        }
    }

    fun loadChat(chatId: String) {
        currentChatId = chatId
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            // Load chat info
            when (val result = chatRepository.getChatById(chatId)) {
                is Result.Success -> {
                    val chat = result.data
                    _uiState.update { state ->
                        state.copy(
                            chatName = chat.name ?: chat.peer?.displayName ?: "Chat",
                            isGroupChat = chat.type.name != "PRIVATE",
                            isOnline = chat.peer?.isOnline ?: false,
                            lastSeen = chat.peer?.lastSeen,
                            isLoading = false
                        )
                    }
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                else -> _uiState.update { it.copy(isLoading = false) }
            }
        }

        // Load messages
        viewModelScope.launch {
            when (val result = chatRepository.getMessages(chatId)) {
                is Result.Success -> _messages.value = result.data.reversed()
                is Result.Error   -> _uiState.update { it.copy(error = result.message) }
                else -> {}
            }
        }

        // Observe real-time messages
        viewModelScope.launch {
            chatRepository.observeMessages(chatId).collect { msgs ->
                if (msgs.isNotEmpty()) _messages.value = msgs.reversed()
            }
        }
    }

    fun sendMessage(chatId: String, text: String) {
        viewModelScope.launch {
            when (val result = chatRepository.sendMessage(chatId, text)) {
                is Result.Success -> {
                    val newMsg = result.data
                    _messages.update { it + newMsg }
                }
                is Result.Error -> _uiState.update { it.copy(error = result.message) }
                else -> {}
            }
        }
    }

    fun onTyping() {
        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            delay(3000)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
