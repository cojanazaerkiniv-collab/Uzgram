package com.uzgram.messenger.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uzgram.messenger.domain.model.Chat
import com.uzgram.messenger.domain.repository.ChatRepository
import com.uzgram.messenger.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatListState(
    val chats: List<Chat> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val totalUnread: Int = 0
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChatListState())
    val state: StateFlow<ChatListState> = _state.asStateFlow()

    init {
        observeChats()
        loadChats()
    }

    private fun observeChats() {
        chatRepository.observeChats()
            .onEach { chats ->
                _state.update { it.copy(chats = chats, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadChats() {
        viewModelScope.launch {
            when (val result = chatRepository.getChats()) {
                is Result.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                else -> {}
            }
        }
    }

    fun onChatLongPress(chat: Chat) {
        // Show context menu: pin, mute, archive, delete
    }

    fun pinChat(chatId: String) { /* call repository */ }
    fun muteChat(chatId: String) { /* call repository */ }
    fun archiveChat(chatId: String) { /* call repository */ }

    fun refresh() {
        _state.update { it.copy(isLoading = true) }
        loadChats()
    }
}
