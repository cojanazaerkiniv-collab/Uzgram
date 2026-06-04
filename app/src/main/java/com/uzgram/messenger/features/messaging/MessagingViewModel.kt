package com.uzgram.messenger.features.messaging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uzgram.messenger.data.local.prefs.SessionPrefs
import com.uzgram.messenger.data.remote.websocket.SocketEvent
import com.uzgram.messenger.data.remote.websocket.SocketManager
import com.uzgram.messenger.domain.model.Chat
import com.uzgram.messenger.domain.model.Message
import com.uzgram.messenger.domain.repository.AuthRepository
import com.uzgram.messenger.domain.repository.ChatRepository
import com.uzgram.messenger.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MessagingState(
    val chat: Chat? = null,
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val inputText: String = "",
    val replyingTo: Message? = null,
    val typingUsers: List<String> = emptyList(),
    val hasMore: Boolean = false,
    val unreadBelow: Int = 0,
    val currentUserId: String = "",
    val error: String? = null
)

@HiltViewModel
class MessagingViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val socketManager: SocketManager,
    private val sessionPrefs: SessionPrefs
) : ViewModel() {

    private val _state = MutableStateFlow(MessagingState())
    val state: StateFlow<MessagingState> = _state.asStateFlow()

    private var currentChatId: String? = null
    private var typingJob: Job? = null
    private var loadMoreCursor: String? = null

    init {
        viewModelScope.launch {
            sessionPrefs.userId.firstOrNull()?.let { userId ->
                _state.update { it.copy(currentUserId = userId) }
            }
        }
        observeSocketEvents()
    }

    fun loadChat(chatId: String) {
        if (currentChatId == chatId) return
        currentChatId = chatId

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Load chat info
            chatRepository.getChatById(chatId).onSuccess { chat ->
                _state.update { it.copy(chat = chat) }
            }

            // Load messages from server
            when (val result = chatRepository.getMessages(chatId)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            messages = result.data,
                            isLoading = false,
                            hasMore = result.data.size >= 50
                        )
                    }
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> {}
            }
        }

        // Observe local messages
        chatRepository.observeMessages(chatId)
            .onEach { messages ->
                _state.update { it.copy(messages = messages, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun loadMoreMessages() {
        val chatId = currentChatId ?: return
        val cursor = loadMoreCursor ?: return
        viewModelScope.launch {
            chatRepository.getMessages(chatId, before = cursor).onSuccess { messages ->
                if (messages.isNotEmpty()) {
                    loadMoreCursor = messages.lastOrNull()?.id
                    _state.update { state ->
                        state.copy(
                            messages = state.messages + messages,
                            hasMore = messages.size >= 50
                        )
                    }
                } else {
                    _state.update { it.copy(hasMore = false) }
                }
            }
        }
    }

    fun onTextChange(text: String) {
        _state.update { it.copy(inputText = text) }

        // Typing indicator debounce
        val chatId = currentChatId ?: return
        if (text.isNotEmpty()) {
            socketManager.sendTypingStart(chatId)
            typingJob?.cancel()
            typingJob = viewModelScope.launch {
                delay(3000)
                socketManager.sendTypingStop(chatId)
            }
        } else {
            typingJob?.cancel()
            socketManager.sendTypingStop(chatId)
        }
    }

    fun sendMessage() {
        val chatId = currentChatId ?: return
        val text = _state.value.inputText.trim()
        if (text.isEmpty()) return

        val replyToId = _state.value.replyingTo?.id
        _state.update { it.copy(inputText = "", replyingTo = null, isSending = true) }

        viewModelScope.launch {
            chatRepository.sendMessage(chatId, text = text, replyToId = replyToId)
            _state.update { it.copy(isSending = false) }
        }
    }

    fun startReply(message: Message) {
        _state.update { it.copy(replyingTo = message) }
    }

    fun cancelReply() {
        _state.update { it.copy(replyingTo = null) }
    }

    fun onMessageLongPress(message: Message) {
        // Emit event to show context menu
    }

    fun react(messageId: String, emoji: String) {
        viewModelScope.launch {
            chatRepository.reactToMessage(messageId, emoji)
        }
    }

    fun startVoiceCall() {
        val userId = _state.value.chat?.otherUser?.id ?: return
        // Navigate to call screen / initiate WebRTC call
    }

    fun startVideoCall() {
        val userId = _state.value.chat?.otherUser?.id ?: return
        // Navigate to video call screen
    }

    private fun observeSocketEvents() {
        socketManager.events
            .onEach { event ->
                when (event) {
                    is SocketEvent.NewMessage -> {
                        if (event.message.chatId == currentChatId) {
                            // Message already inserted via DB observer
                        }
                    }
                    is SocketEvent.TypingIndicator -> {
                        if (event.chatId == currentChatId) {
                            _state.update { state ->
                                val current = state.typingUsers.toMutableList()
                                if (event.isTyping) {
                                    if (!current.contains(event.userId)) current.add(event.userId)
                                } else {
                                    current.remove(event.userId)
                                }
                                state.copy(typingUsers = current)
                            }
                        }
                    }
                    is SocketEvent.MessageEdited -> {
                        // DB observer will handle update
                    }
                    is SocketEvent.MessageDeleted -> {
                        if (event.chatId == currentChatId) {
                            _state.update { state ->
                                state.copy(messages = state.messages.filter { it.id != event.messageId })
                            }
                        }
                    }
                    else -> {}
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        currentChatId?.let { socketManager.sendTypingStop(it) }
    }
}
