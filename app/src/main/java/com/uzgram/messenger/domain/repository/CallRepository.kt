package com.uzgram.messenger.domain.repository

import android.content.Context
import com.uzgram.messenger.domain.model.CallRecord
import com.uzgram.messenger.domain.model.CallState
import com.uzgram.messenger.domain.model.CallType
import kotlinx.coroutines.flow.Flow

interface CallRepository {
    // ── Call history ──────────────────────────────────────────────────────────
    fun getCallHistory(): Flow<List<CallRecord>>
    suspend fun getCallById(callId: String): CallRecord
    suspend fun deleteCallRecord(callId: String)
    suspend fun clearAllCallHistory()
    suspend fun missedCallCount(): Int
    fun observeMissedCallCount(): Flow<Int>

    // ── WebRTC call lifecycle ─────────────────────────────────────────────────
    suspend fun initiateCall(targetUserId: String, callType: CallType): String
    suspend fun acceptCall(callId: String)
    suspend fun declineCall(callId: String)
    suspend fun endCall(callId: String)
    fun observeCallState(callId: String): Flow<CallState>

    // ── Media controls ────────────────────────────────────────────────────────
    suspend fun setMuted(muted: Boolean)
    fun setSpeaker(context: Context, enabled: Boolean)
    fun setCameraEnabled(enabled: Boolean)
    fun switchCamera(useFrontCamera: Boolean)
}
