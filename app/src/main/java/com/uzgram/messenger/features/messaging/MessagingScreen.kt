package com.uzgram.messenger.features.messaging

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.uzgram.messenger.domain.model.Message
import com.uzgram.messenger.domain.model.MessageType
import com.uzgram.messenger.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingScreen(
    chatId: String,
    onBack: () -> Unit,
    onUserClick: (String) -> Unit,
    viewModel: MessagingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val lazyListState = rememberLazyListState()

    LaunchedEffect(chatId) {
        viewModel.loadChat(chatId)
    }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            MessagingTopBar(
                state = state,
                onBack = onBack,
                onUserClick = { state.chat?.otherUser?.id?.let(onUserClick) },
                onCallClick = { viewModel.startVoiceCall() },
                onVideoCallClick = { viewModel.startVideoCall() }
            )
        },
        bottomBar = {
            MessageInputBar(
                text = state.inputText,
                onTextChange = { viewModel.onTextChange(it) },
                onSend = { viewModel.sendMessage() },
                onAttachClick = { /* show media picker */ },
                onVoiceRecord = { /* start voice recording */ },
                replyTo = state.replyingTo,
                onCancelReply = { viewModel.cancelReply() },
                isSending = state.isSending
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = UzBlue)
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    reverseLayout = true
                ) {
                    itemsIndexed(
                        items = state.messages,
                        key = { _, msg -> msg.id }
                    ) { index, message ->
                        val prevMessage = state.messages.getOrNull(index + 1)
                        val nextMessage = state.messages.getOrNull(index - 1)
                        val isFirstInGroup = prevMessage?.senderId != message.senderId
                        val isLastInGroup = nextMessage?.senderId != message.senderId
                        val isMine = message.senderId == state.currentUserId

                        MessageBubble(
                            message = message,
                            isMine = isMine,
                            isFirstInGroup = isFirstInGroup,
                            isLastInGroup = isLastInGroup,
                            showAvatar = !isMine && isLastInGroup,
                            onLongPress = { viewModel.onMessageLongPress(message) },
                            onReply = { viewModel.startReply(message) },
                            onReact = { emoji -> viewModel.react(message.id, emoji) }
                        )
                    }

                    if (state.hasMore) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = UzBlue, modifier = Modifier.size(24.dp))
                            }
                            LaunchedEffect(Unit) { viewModel.loadMoreMessages() }
                        }
                    }
                }

                // Typing indicator
                AnimatedVisibility(
                    visible = state.typingUsers.isNotEmpty(),
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically(),
                    modifier = Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 8.dp)
                ) {
                    TypingIndicator(users = state.typingUsers)
                }

                // Scroll to bottom FAB
                AnimatedVisibility(
                    visible = !lazyListState.isScrolledToTop() && state.unreadBelow > 0,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 8.dp)
                ) {
                    FloatingActionButton(
                        onClick = { /* scroll to bottom */ },
                        modifier = Modifier.size(44.dp),
                        containerColor = UzBlue
                    ) {
                        BadgedBox(badge = {
                            if (state.unreadBelow > 0) Badge { Text("${state.unreadBelow}") }
                        }) {
                            Icon(Icons.Filled.KeyboardArrowDown, null, tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessagingTopBar(
    state: MessagingState,
    onBack: () -> Unit,
    onUserClick: () -> Unit,
    onCallClick: () -> Unit,
    onVideoCallClick: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "Back")
            }
        },
        title = {
            Row(
                modifier = Modifier.clickable(onClick = onUserClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val chat = state.chat ?: return@Row
                Box(modifier = Modifier.size(38.dp)) {
                    if (chat.displayAvatar != null) {
                        AsyncImage(
                            model = chat.displayAvatar,
                            contentDescription = chat.displayName,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                                .background(UzBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(chat.displayName.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (chat.isOnline) {
                        Box(
                            modifier = Modifier.size(10.dp).align(Alignment.BottomEnd)
                                .background(Color.White, CircleShape).padding(1.5.dp)
                                .background(Color(0xFF4CAF50), CircleShape)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(chat.displayName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        text = when {
                            state.typingUsers.isNotEmpty() -> "typing..."
                            chat.isOnline -> "online"
                            chat.lastMessage != null -> "last seen recently"
                            else -> ""
                        },
                        fontSize = 12.sp,
                        color = if (state.typingUsers.isNotEmpty() || chat.isOnline) UzBlue
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onCallClick) { Icon(Icons.Outlined.Call, "Voice call") }
            IconButton(onClick = onVideoCallClick) { Icon(Icons.Outlined.Videocam, "Video call") }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
    )
}

@Composable
fun MessageBubble(
    message: Message,
    isMine: Boolean,
    isFirstInGroup: Boolean,
    isLastInGroup: Boolean,
    showAvatar: Boolean,
    onLongPress: () -> Unit,
    onReply: () -> Unit,
    onReact: (String) -> Unit
) {
    val bubbleShape = if (isMine) MessageBubbleShapeSent else MessageBubbleShapeReceived
    val bubbleColor = if (isMine) UzBlue else MaterialTheme.colorScheme.surface
    val textColor = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier.fillMaxWidth().padding(
            start = if (isMine) 60.dp else 0.dp,
            end = if (isMine) 0.dp else 60.dp,
            top = if (isFirstInGroup) 6.dp else 2.dp,
            bottom = if (isLastInGroup) 4.dp else 1.dp
        ),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isMine) {
            if (showAvatar) {
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (message.senderAvatarUrl != null) {
                        AsyncImage(model = message.senderAvatarUrl, contentDescription = null,
                            modifier = Modifier.fillMaxSize())
                    } else {
                        Text(message.senderName.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
            } else {
                Spacer(modifier = Modifier.width(38.dp))
            }
        }

        Column(horizontalAlignment = if (isMine) Alignment.End else Alignment.Start) {
            if (!isMine && isFirstInGroup && message.chatId.startsWith("group")) {
                Text(
                    text = message.senderName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = UzBlue,
                    modifier = Modifier.padding(start = 12.dp, bottom = 2.dp)
                )
            }

            // Reply preview
            message.replyToText?.let { replyText ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(if (isMine) UzBlueDeep else MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = replyText,
                        fontSize = 12.sp,
                        color = if (isMine) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Message bubble
            Box(
                modifier = Modifier
                    .clip(bubbleShape)
                    .background(bubbleColor)
                    .combinedClickable(
                        onClick = {},
                        onLongClick = onLongPress
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                when (message.type) {
                    MessageType.TEXT -> {
                        Column {
                            Text(
                                text = message.text ?: "",
                                color = textColor,
                                fontSize = 15.sp
                            )
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.align(Alignment.End).padding(top = 2.dp)
                            ) {
                                if (message.isEdited) {
                                    Text(
                                        "edited ",
                                        fontSize = 10.sp,
                                        color = textColor.copy(alpha = 0.6f)
                                    )
                                }
                                Text(
                                    text = formatMessageTime(message.createdAt),
                                    fontSize = 11.sp,
                                    color = textColor.copy(alpha = 0.7f)
                                )
                                if (isMine) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    MessageStatusIcon(message)
                                }
                            }
                        }
                    }
                    MessageType.IMAGE -> {
                        Column {
                            AsyncImage(
                                model = message.mediaUrl,
                                contentDescription = null,
                                modifier = Modifier.size(220.dp, 180.dp).clip(RoundedCornerShape(8.dp))
                            )
                            message.text?.let {
                                Text(it, color = textColor, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                    else -> {
                        Text(message.text ?: message.fileName ?: "Message", color = textColor)
                    }
                }
            }

            // Reactions
            if (message.reactions.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    message.reactions.forEach { reaction ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(reaction.emoji, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${reaction.count}", fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageStatusIcon(message: Message) {
    // Show read/delivered/sent status ticks
    Icon(
        imageVector = Icons.Filled.DoneAll,
        contentDescription = null,
        modifier = Modifier.size(14.dp),
        tint = if (message.readBy.isNotEmpty()) Color(0xFF4FC3F7) else Color.White.copy(0.6f)
    )
}

@Composable
fun MessageInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachClick: () -> Unit,
    onVoiceRecord: () -> Unit,
    replyTo: Message?,
    onCancelReply: () -> Unit,
    isSending: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        AnimatedVisibility(visible = replyTo != null) {
            replyTo?.let { msg ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.width(3.dp).height(36.dp)
                            .background(UzBlue, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Reply to ${msg.senderName}", fontSize = 12.sp, color = UzBlue, fontWeight = FontWeight.SemiBold)
                        Text(msg.text ?: "Message", fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onCancelReply, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.Close, null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            IconButton(onClick = onAttachClick) {
                Icon(Icons.Outlined.AttachFile, "Attach", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { Text("Message...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier.weight(1f),
                maxLines = 5,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedBorderColor = UzBlue
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Send or mic button
            FilledIconButton(
                onClick = if (text.isNotBlank()) onSend else onVoiceRecord,
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = UzBlue)
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else if (text.isNotBlank()) {
                    Icon(Icons.Filled.Send, "Send", tint = Color.White)
                } else {
                    Icon(Icons.Filled.Mic, "Voice", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun TypingIndicator(users: List<String>) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("...", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (users.size == 1) "${users[0]} is typing"
                       else "${users.joinToString(", ")} are typing",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun LazyListState.isScrolledToTop(): Boolean =
    firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0

private fun formatMessageTime(timestamp: String): String {
    return try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
        val date = sdf.parse(timestamp.take(19)) ?: return ""
        java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(date)
    } catch (e: Exception) { "" }
}
