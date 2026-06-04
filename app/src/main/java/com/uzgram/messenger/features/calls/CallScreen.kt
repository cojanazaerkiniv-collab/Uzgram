package com.uzgram.messenger.features.calls

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.uzgram.messenger.domain.model.CallState
import com.uzgram.messenger.domain.model.CallType
import kotlinx.coroutines.delay

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CallScreen(
    callId: String,
    remoteUserId: String,
    remoteUserName: String,
    remoteAvatarUrl: String?,
    callType: CallType,
    isIncoming: Boolean,
    viewModel: CallViewModel = hiltViewModel(),
    onCallEnded: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val permissions = rememberMultiplePermissionsState(
        permissions = buildList {
            add(Manifest.permission.RECORD_AUDIO)
            if (callType == CallType.VIDEO) add(Manifest.permission.CAMERA)
        }
    )

    LaunchedEffect(callId) {
        permissions.launchMultiplePermissionRequest()
        if (isIncoming) {
            viewModel.handleIncomingCall(callId, remoteUserId, callType)
        } else {
            viewModel.initiateCall(remoteUserId, callType)
        }
    }

    LaunchedEffect(state.isEnded) {
        if (state.isEnded) onCallEnded()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
    ) {
        if (callType == CallType.VIDEO && state.callState == CallState.CONNECTED) {
            VideoCallContent(state = state, viewModel = viewModel)
        } else {
            VoiceCallContent(
                remoteUserName = remoteUserName,
                remoteAvatarUrl = remoteAvatarUrl,
                state = state
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CallStatusBadge(state = state)
            if (state.callState == CallState.CONNECTED) {
                CallDurationTimer(startedAt = state.connectedAt)
            }
        }

        CallControls(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp),
            state = state,
            callType = callType,
            isIncoming = isIncoming,
            viewModel = viewModel,
            onCallEnded = onCallEnded
        )
    }
}

@Composable
private fun VideoCallContent(
    state: CallUiState,
    viewModel: CallViewModel
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1A1A2E))
        ) {
            Text(
                "Remote Video",
                color = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(120.dp, 180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF2D2D3D))
        ) {
            if (!state.isCameraOff) {
                Text(
                    "Local Video",
                    color = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 11.sp
                )
            } else {
                Icon(
                    Icons.Filled.VideocamOff,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun VoiceCallContent(
    remoteUserName: String,
    remoteAvatarUrl: String?,
    state: CallUiState
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .scale(if (state.callState == CallState.RINGING) pulseScale else 1f)
                .background(
                    color = Color.White.copy(alpha = 0.08f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (remoteAvatarUrl != null) {
                AsyncImage(
                    model = remoteAvatarUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(Color(0xFF3B82F6), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        remoteUserName.take(1).uppercase(),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            remoteUserName,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = when (state.callState) {
                CallState.CONNECTING  -> "Ulanmoqda..."
                CallState.RINGING     -> "Qo'ng'iroq qilinmoqda..."
                CallState.CONNECTED   -> ""
                CallState.RECONNECTING-> "Qayta ulanmoqda..."
                CallState.ENDED       -> "Qo'ng'iroq tugadi"
            },
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun CallStatusBadge(state: CallUiState) {
    AnimatedVisibility(
        visible = state.networkQuality != null && state.callState == CallState.CONNECTED,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = when {
                state.networkQuality == "poor" -> Color.Red.copy(alpha = 0.85f)
                state.networkQuality == "fair" -> Color(0xFFF59E0B).copy(alpha = 0.85f)
                else -> Color(0xFF10B981).copy(alpha = 0.85f)
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(
                text = when (state.networkQuality) {
                    "poor" -> "Zaiif signal"
                    "fair" -> "O'rtacha signal"
                    else  -> "Yaxshi signal"
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CallDurationTimer(startedAt: Long) {
    var elapsed by remember { mutableLongStateOf(0L) }

    LaunchedEffect(startedAt) {
        while (true) {
            elapsed = (System.currentTimeMillis() - startedAt) / 1000
            delay(1000)
        }
    }

    val h = elapsed / 3600
    val m = (elapsed % 3600) / 60
    val s = elapsed % 60

    Text(
        text = if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s),
        fontSize = 16.sp,
        color = Color.White.copy(alpha = 0.8f),
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(top = 6.dp)
    )
}

@Composable
private fun CallControls(
    modifier: Modifier = Modifier,
    state: CallUiState,
    callType: CallType,
    isIncoming: Boolean,
    viewModel: CallViewModel,
    onCallEnded: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (state.callState == CallState.RINGING && isIncoming) {
            IncomingCallControls(
                onAccept = { viewModel.acceptCall() },
                onDecline = { viewModel.declineCall(); onCallEnded() }
            )
        } else {
            ActiveCallControls(
                state = state,
                callType = callType,
                viewModel = viewModel,
                onHangup = { viewModel.endCall(); onCallEnded() }
            )
        }
    }
}

@Composable
private fun IncomingCallControls(
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
                onClick = onDecline,
                modifier = Modifier
                    .size(72.dp)
                    .background(Color.Red, CircleShape)
            ) {
                Icon(Icons.Rounded.CallEnd, contentDescription = "Rad etish", tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text("Rad etish", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
                onClick = onAccept,
                modifier = Modifier
                    .size(72.dp)
                    .background(Color(0xFF10B981), CircleShape)
            ) {
                Icon(Icons.Rounded.Call, contentDescription = "Qabul qilish", tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text("Qabul qilish", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        }
    }
}

@Composable
private fun ActiveCallControls(
    state: CallUiState,
    callType: CallType,
    viewModel: CallViewModel,
    onHangup: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CallControlButton(
            icon = if (state.isMuted) Icons.Rounded.MicOff else Icons.Rounded.Mic,
            label = if (state.isMuted) "Ovoz yoq" else "Ovoz",
            active = state.isMuted,
            onClick = { viewModel.toggleMute() }
        )

        CallControlButton(
            icon = if (state.isSpeakerOn) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeDown,
            label = "Karnay",
            active = state.isSpeakerOn,
            onClick = { viewModel.toggleSpeaker() }
        )

        IconButton(
            onClick = onHangup,
            modifier = Modifier
                .size(72.dp)
                .background(Color.Red, CircleShape)
        ) {
            Icon(
                Icons.Rounded.CallEnd,
                contentDescription = "Tugatish",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        if (callType == CallType.VIDEO) {
            CallControlButton(
                icon = if (state.isCameraOff) Icons.Rounded.VideocamOff else Icons.Rounded.Videocam,
                label = "Kamera",
                active = state.isCameraOff,
                onClick = { viewModel.toggleCamera() }
            )
        }

        CallControlButton(
            icon = Icons.Rounded.Cameraswitch,
            label = "Almashtir",
            active = false,
            onClick = { viewModel.switchCamera() }
        )
    }
}

@Composable
private fun CallControlButton(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = if (active) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.1f),
                    shape = CircleShape
                )
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
    }
}
