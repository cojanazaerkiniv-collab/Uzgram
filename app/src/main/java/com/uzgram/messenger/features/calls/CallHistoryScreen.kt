package com.uzgram.messenger.features.calls

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.uzgram.messenger.domain.model.CallDirection
import com.uzgram.messenger.domain.model.CallRecord
import com.uzgram.messenger.domain.model.CallStatus
import com.uzgram.messenger.domain.model.CallType
import com.uzgram.messenger.ui.components.AvatarImage
import com.uzgram.messenger.ui.theme.UzBlue
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallHistoryScreen(
    onStartCall: (userId: String, isVideo: Boolean) -> Unit,
    onCallClick: (CallRecord) -> Unit,
    viewModel: CallHistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Qo'ng'iroqlar", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { /* New call */ }) {
                        Icon(Icons.Outlined.AddCall, contentDescription = "Yangi qo'ng'iroq")
                    }
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(Icons.Outlined.DeleteSweep, contentDescription = "Tozalash")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* new call */ },
                containerColor = UzBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.AddCall, contentDescription = "Yangi qo'ng'iroq")
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.calls.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.PhoneMissed,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Qo'ng'iroqlar tarixi yo'q", fontSize = 17.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Do'stlaringizga qo'ng'iroq qiling", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(state.calls, key = { it.id }) { call ->
                    CallHistoryItem(
                        call = call,
                        onClick = { onCallClick(call) },
                        onCallBack = { onStartCall(call.peerId, call.type == CallType.VIDEO) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 78.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Tarixni tozalash") },
            text = { Text("Barcha qo'ng'iroqlar tarixi o'chiriladi. Bu amalni qaytarib bo'lmaydi.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.clearHistory(); showClearDialog = false }
                ) {
                    Text("O'chirish", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Bekor qilish") }
            }
        )
    }
}

@Composable
private fun CallHistoryItem(
    call: CallRecord,
    onClick: () -> Unit,
    onCallBack: () -> Unit
) {
    val statusColor = when {
        call.status == CallStatus.MISSED && call.direction == CallDirection.INCOMING -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarImage(
            url = call.peerAvatarUrl,
            fallbackName = call.peerName,
            size = 52.dp,
            modifier = Modifier.clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = call.peerName,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = statusColor
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    imageVector = callDirectionIcon(call),
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = callStatusText(call),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("·", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = formatCallTime(call.startedAt),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Call back button
        IconButton(onClick = onCallBack) {
            Icon(
                imageVector = if (call.type == CallType.VIDEO || call.type == CallType.GROUP_VIDEO)
                    Icons.Outlined.Videocam else Icons.Outlined.Call,
                contentDescription = "Qayta qo'ng'iroq",
                tint = UzBlue,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

private fun callDirectionIcon(call: CallRecord): ImageVector {
    return when {
        call.direction == CallDirection.INCOMING && call.status == CallStatus.MISSED -> Icons.Filled.CallMissedOutgoing
        call.direction == CallDirection.INCOMING -> Icons.Filled.CallReceived
        else -> Icons.Filled.CallMade
    }
}

private fun callStatusText(call: CallRecord): String {
    return when (call.status) {
        CallStatus.MISSED -> "O'tkazib yuborilgan"
        CallStatus.DECLINED -> "Rad etilgan"
        CallStatus.BUSY -> "Band"
        CallStatus.FAILED -> "Muvaffaqiyatsiz"
        CallStatus.COMPLETED -> {
            val secs = call.durationSeconds
            if (secs < 60) "${secs}s" else "${secs / 60}m ${secs % 60}s"
        }
    }
}

private fun formatCallTime(instant: Instant): String {
    val formatter = DateTimeFormatter.ofPattern("d MMM, HH:mm").withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}
