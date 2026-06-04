package com.uzgram.messenger.features.calls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uzgram.messenger.domain.model.CallRecord
import com.uzgram.messenger.domain.repository.CallRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CallHistoryUiState(
    val isLoading: Boolean = false,
    val calls: List<CallRecord> = emptyList(),
    val missedCount: Int = 0,
    val error: String? = null
)

@HiltViewModel
class CallHistoryViewModel @Inject constructor(
    private val callRepository: CallRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CallHistoryUiState())
    val state: StateFlow<CallHistoryUiState> = _state.asStateFlow()

    init {
        loadCalls()
        observeMissedCount()
    }

    private fun loadCalls() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            callRepository.getCallHistory()
                .catch { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
                .collect { calls -> _state.update { it.copy(calls = calls, isLoading = false) } }
        }
    }

    private fun observeMissedCount() {
        viewModelScope.launch {
            callRepository.observeMissedCallCount()
                .catch { }
                .collect { count -> _state.update { it.copy(missedCount = count) } }
        }
    }

    fun deleteRecord(callId: String) {
        viewModelScope.launch {
            try { callRepository.deleteCallRecord(callId) } catch (_: Exception) {}
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            try { callRepository.clearAllCallHistory() } catch (_: Exception) {}
        }
    }
}
