package com.uzgram.messenger.data.repository

import android.content.Context
import android.media.AudioManager
import com.uzgram.messenger.data.local.UzGramDatabase
import com.uzgram.messenger.data.remote.ApiService
import com.uzgram.messenger.data.remote.websocket.SocketManager
import com.uzgram.messenger.domain.model.*
import com.uzgram.messenger.domain.repository.CallRepository
import com.uzgram.messenger.utils.Result
import com.uzgram.messenger.utils.safeApiCall
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val db: UzGramDatabase,
    private val socketManager: SocketManager
) : CallRepository {

    // ── Call history ──────────────────────────────────────────────────────────

    override fun getCallHistory(): Flow<List<CallRecord>> = flow {
        when (val r = safeApiCall { api.getCallHistory() }) {
            is Result.Success -> emit(r.data.map { it.toCallRecord() })
            is Result.Error   -> throw Exception(r.message)
            else -> {}
        }
    }

    override suspend fun getCallById(callId: String): CallRecord {
        return when (val r = safeApiCall { api.getCallById(callId) }) {
            is Result.Success -> r.data.toCallRecord()
            is Result.Error   -> throw Exception(r.message)
            else              -> throw Exception("Unknown error")
        }
    }

    override suspend fun deleteCallRecord(callId: String) {
        when (val r = safeApiCall { api.deleteCallRecord(callId) }) {
            is Result.Error -> throw Exception(r.message)
            else -> {}
        }
    }

    override suspend fun clearAllCallHistory() {
        when (val r = safeApiCall { api.clearCallHistory() }) {
            is Result.Error -> throw Exception(r.message)
            else -> {}
        }
    }

    override suspend fun missedCallCount(): Int {
        return when (val r = safeApiCall { api.getMissedCallCount() }) {
            is Result.Success -> r.data["count"]?.toString()?.toIntOrNull() ?: 0
            else              -> 0
        }
    }

    override fun observeMissedCallCount(): Flow<Int> = flow {
        emit(missedCallCount())
    }

    // ── WebRTC call lifecycle ─────────────────────────────────────────────────

    override suspend fun initiateCall(targetUserId: String, callType: CallType): String {
        val body = mapOf(
            "target_user_id" to targetUserId,
            "call_type" to callType.name.lowercase()
        )
        return when (val r = safeApiCall { api.initiateCall(body) }) {
            is Result.Success -> r.data["call_id"]?.toString()
                ?: throw Exception("Missing call_id in response")
            is Result.Error   -> throw Exception(r.message)
            else              -> throw Exception("Unknown error")
        }
    }

    override suspend fun acceptCall(callId: String) {
        when (val r = safeApiCall { api.acceptCall(callId) }) {
            is Result.Error -> throw Exception(r.message)
            else -> {}
        }
    }

    override suspend fun declineCall(callId: String) {
        when (val r = safeApiCall { api.declineCall(callId) }) {
            is Result.Error -> throw Exception(r.message)
            else -> {}
        }
    }

    override suspend fun endCall(callId: String) {
        when (val r = safeApiCall { api.endCall(callId, emptyMap()) }) {
            is Result.Error -> throw Exception(r.message)
            else -> {}
        }
        socketManager.endCall(callId)
    }

    override fun observeCallState(callId: String): Flow<CallState> {
        return socketManager.events
            .filterIsInstance<com.uzgram.messenger.data.remote.websocket.SocketEvent.CallStateChanged>()
            .filter { it.callId == callId }
            .map { event ->
                when (event.state) {
                    "ringing"      -> CallState.RINGING
                    "connected"    -> CallState.CONNECTED
                    "reconnecting" -> CallState.RECONNECTING
                    "ended",
                    "declined",
                    "missed"       -> CallState.ENDED
                    else           -> CallState.CONNECTING
                }
            }
            .onStart { emit(CallState.RINGING) }
    }

    // ── Media controls ────────────────────────────────────────────────────────

    override suspend fun setMuted(muted: Boolean) {
        // Signal mute state through socket
        socketManager.sendMuteState(muted)
    }

    override fun setSpeaker(context: Context, enabled: Boolean) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isSpeakerphoneOn = enabled
        audioManager.mode = if (enabled) AudioManager.MODE_IN_COMMUNICATION else AudioManager.MODE_NORMAL
    }

    override fun setCameraEnabled(enabled: Boolean) {
        // Delegate to WebRTC engine in production
    }

    override fun switchCamera(useFrontCamera: Boolean) {
        // Delegate to WebRTC engine in production
    }
}

private fun Map<String, Any?>.toCallRecord(): CallRecord {
    fun str(key: String) = this[key]?.toString() ?: ""
    fun int(key: String) = str(key).toIntOrNull() ?: 0

    val dir = if (str("direction") == "incoming") CallDirection.INCOMING else CallDirection.OUTGOING
    val status = when (str("status")) {
        "declined"  -> CallStatus.DECLINED
        "busy"      -> CallStatus.BUSY
        "completed" -> CallStatus.COMPLETED
        "failed"    -> CallStatus.FAILED
        else        -> CallStatus.MISSED
    }
    val type = when (str("call_type")) {
        "video"       -> CallType.VIDEO
        "group_voice" -> CallType.GROUP_VOICE
        "group_video" -> CallType.GROUP_VIDEO
        else          -> CallType.VOICE
    }

    return CallRecord(
        id = str("id"),
        peerId = str("peer_id"),
        peerName = str("peer_name"),
        peerUsername = str("peer_username"),
        peerAvatarUrl = this["peer_avatar_url"]?.toString(),
        type = type,
        direction = dir,
        status = status,
        durationSeconds = int("duration_seconds"),
        startedAt = runCatching { Instant.parse(str("started_at")) }.getOrDefault(Instant.now()),
        endedAt = this["ended_at"]?.let { runCatching { Instant.parse(it.toString()) }.getOrNull() },
        isGroupCall = str("is_group_call") == "true",
        groupName = this["group_name"]?.toString(),
        participantCount = int("participant_count")
    )
}
