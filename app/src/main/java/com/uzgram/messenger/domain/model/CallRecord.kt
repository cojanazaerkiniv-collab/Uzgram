package com.uzgram.messenger.domain.model

import java.time.Instant

enum class CallType { VOICE, VIDEO, GROUP_VOICE, GROUP_VIDEO }
enum class CallDirection { INCOMING, OUTGOING }
enum class CallStatus { MISSED, DECLINED, BUSY, COMPLETED, FAILED }

enum class CallState {
    CONNECTING,
    RINGING,
    CONNECTED,
    RECONNECTING,
    ENDED
}

data class CallRecord(
    val id: String,
    val peerId: String,
    val peerName: String,
    val peerUsername: String,
    val peerAvatarUrl: String?,
    val type: CallType,
    val direction: CallDirection,
    val status: CallStatus,
    val durationSeconds: Int,
    val startedAt: Instant,
    val endedAt: Instant?,
    val isGroupCall: Boolean,
    val groupName: String?,
    val participantCount: Int
)

data class ActiveCall(
    val callId: String,
    val peerId: String,
    val peerName: String,
    val peerAvatarUrl: String?,
    val type: CallType,
    val direction: CallDirection,
    val isMuted: Boolean,
    val isSpeakerOn: Boolean,
    val isCameraOn: Boolean,
    val isScreenSharing: Boolean,
    val isBackCamera: Boolean,
    val isConnected: Boolean,
    val durationSeconds: Int,
    val signalQuality: Int
)
