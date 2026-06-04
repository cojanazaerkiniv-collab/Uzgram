package com.uzgram.messenger.features.calls

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uzgram.messenger.domain.model.CallRecord
import com.uzgram.messenger.domain.model.CallState
import com.uzgram.messenger.domain.model.CallType
import com.uzgram.messenger.domain.repository.CallRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CallUiState(
    val callState: CallState = CallState.CONNECTING,
    val isEnded: Boolean = false,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isCameraOff: Boolean = false,
    val isFrontCamera: Boolean = true,
    val networkQuality: String? = null,
    val connectedAt: Long = 0L,
    val durationSeconds: Long = 0L,
    val error: String? = null
)

@HiltViewModel
class CallViewModel @Inject constructor(
    private val callRepository: CallRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(CallUiState())
    val state: StateFlow<CallUiState> = _state.asStateFlow()

    private var currentCallId: String? = null
    private var remoteUserId: String? = null

    fun initiateCall(targetUserId: String, callType: CallType) {
        remoteUserId = targetUserId
        viewModelScope.launch {
            _state.update { it.copy(callState = CallState.RINGING) }
            try {
                val callId = callRepository.initiateCall(targetUserId, callType)
                currentCallId = callId
                observeCallState(callId)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isEnded = true) }
            }
        }
    }

    fun handleIncomingCall(callId: String, fromUserId: String, callType: CallType) {
        currentCallId = callId
        remoteUserId = fromUserId
        _state.update { it.copy(callState = CallState.RINGING) }
        observeCallState(callId)
    }

    private fun observeCallState(callId: String) {
        viewModelScope.launch {
            callRepository.observeCallState(callId)
                .catch { e -> _state.update { it.copy(error = e.message, isEnded = true) } }
                .collect { newState ->
                    when (newState) {
                        CallState.CONNECTED -> {
                            _state.update {
                                it.copy(callState = newState, connectedAt = System.currentTimeMillis())
                            }
                        }
                        CallState.ENDED -> {
                            _state.update { it.copy(callState = newState, isEnded = true) }
                        }
                        else -> _state.update { it.copy(callState = newState) }
                    }
                }
        }
    }

    fun acceptCall() {
        val callId = currentCallId ?: return
        viewModelScope.launch {
            try {
                callRepository.acceptCall(callId)
                _state.update { it.copy(callState = CallState.CONNECTING) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun declineCall() {
        val callId = currentCallId ?: return
        viewModelScope.launch {
            try {
                callRepository.declineCall(callId)
            } catch (_: Exception) {}
            _state.update { it.copy(isEnded = true) }
        }
    }

    fun endCall() {
        val callId = currentCallId ?: return
        viewModelScope.launch {
            try {
                callRepository.endCall(callId)
            } catch (_: Exception) {}
            _state.update { it.copy(isEnded = true) }
        }
    }

    fun toggleMute() {
        val muted = !_state.value.isMuted
        viewModelScope.launch {
            callRepository.setMuted(muted)
        }
        _state.update { it.copy(isMuted = muted) }
    }

    fun toggleSpeaker() {
        val speaker = !_state.value.isSpeakerOn
        callRepository.setSpeaker(context, speaker)
        _state.update { it.copy(isSpeakerOn = speaker) }
    }

    fun toggleCamera() {
        val off = !_state.value.isCameraOff
        callRepository.setCameraEnabled(!off)
        _state.update { it.copy(isCameraOff = off) }
    }

    fun switchCamera() {
        val front = !_state.value.isFrontCamera
        callRepository.switchCamera(front)
        _state.update { it.copy(isFrontCamera = front) }
    }

    override fun onCleared() {
        super.onCleared()
        val callId = currentCallId
        if (callId != null && !_state.value.isEnded) {
            viewModelScope.launch {
                try { callRepository.endCall(callId) } catch (_: Exception) {}
            }
        }
    }
}
