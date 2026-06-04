package com.uzgram.messenger.features.chat

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.uzgram.messenger.domain.model.Chat
import com.uzgram.messenger.domain.model.ChatType
import com.uzgram.messenger.ui.theme.UzBlue
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onChatClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onNewChatClick: () -> Unit,
    viewModel: ChatListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (showSearch) {
                SearchTopBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClose = { showSearch = false; searchQuery = "" }
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            "UzGram",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    },
                    actions = {
                        IconButton(onClick = { showSearch = true }) {
                            Icon(Icons.Outlined.Search, "Search")
                        }
                        IconButton(onClick = { /* Edit/compose menu */ }) {
                            Icon(Icons.Outlined.Edit, "New chat")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewChatClick,
                containerColor = UzBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Edit, "New Chat")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading && state.chats.isEmpty()) {
                ChatListSkeleton()
            } else if (state.chats.isEmpty()) {
                EmptyChatList(onNewChatClick)
            } else {
                val filteredChats = if (searchQuery.isNotEmpty()) {
                    state.chats.filter { chat ->
                        chat.displayName.contains(searchQuery, ignoreCase = true) ||
                                chat.lastMessage?.text?.contains(searchQuery, ignoreCase = true) == true
                    }
                } else state.chats

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(
                        items = filteredChats,
                        key = { it.id }
                    ) { chat ->
                        ChatListItem(
                            chat = chat,
                            onClick = { onChatClick(chat.id) },
                            onLongClick = { viewModel.onChatLongPress(chat) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatListItem(
    chat: Chat,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(modifier = Modifier.size(56.dp)) {
            if (chat.displayAvatar != null) {
                AsyncImage(
                    model = chat.displayAvatar,
                    contentDescription = chat.displayName,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(avatarColor(chat.displayName)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = chat.displayName.take(2).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }

            // Online indicator
            if (chat.isOnline) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.BottomEnd)
                        .background(Color.White, CircleShape)
                        .padding(2.dp)
                        .background(Color(0xFF4CAF50), CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = chat.displayName,
                        fontWeight = if (chat.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    when (chat.type) {
                        ChatType.CHANNEL -> {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Filled.Campaign, null, modifier = Modifier.size(14.dp),
                                tint = UzBlue)
                        }
                        ChatType.GROUP -> {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Filled.Group, null, modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        else -> {}
                    }
                    if (chat.isMuted) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Filled.VolumeOff, null, modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                chat.lastMessage?.let {
                    Text(
                        text = formatTime(it.createdAt),
                        fontSize = 12.sp,
                        color = if (chat.unreadCount > 0) UzBlue
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.lastMessage?.let { msg ->
                        when {
                            msg.text != null -> msg.text
                            msg.mediaType?.startsWith("image") == true -> "Photo"
                            msg.mediaType?.startsWith("video") == true -> "Video"
                            msg.fileName != null -> msg.fileName
                            else -> "Message"
                        }
                    } ?: "No messages yet",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (chat.unreadCount > 0 && !chat.isMuted) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Badge(
                        containerColor = UzBlue
                    ) {
                        Text(
                            text = if (chat.unreadCount > 99) "99+" else chat.unreadCount.toString(),
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                } else if (chat.unreadCount > 0 && chat.isMuted) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Badge(
                        containerColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Text(
                            text = if (chat.unreadCount > 99) "99+" else chat.unreadCount.toString(),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    TopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search chats...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.ArrowBack, "Close search")
            }
        }
    )
}

@Composable
private fun EmptyChatList(onNewChat: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.ChatBubbleOutline,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("No conversations yet", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Start a new chat to connect with friends",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onNewChat,
            colors = ButtonDefaults.buttonColors(containerColor = UzBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Add, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("New Chat")
        }
    }
}

@Composable
private fun ChatListSkeleton() {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(8) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier.width(120.dp).height(16.dp).clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier.width(200.dp).height(12.dp).clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }
        }
    }
}

private fun avatarColor(name: String): Color {
    val colors = listOf(
        Color(0xFF2AABEE), Color(0xFF4CAF50), Color(0xFFFF9500),
        Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF00BCD4),
        Color(0xFFFF5722), Color(0xFF607D8B)
    )
    return colors[name.hashCode().and(0x7FFFFFFF) % colors.size]
}

private fun formatTime(timestamp: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = sdf.parse(timestamp.take(19)) ?: return ""
        val now = Date()
        val diff = now.time - date.time
        when {
            diff < 60_000 -> "now"
            diff < 3_600_000 -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            diff < 86_400_000 -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            diff < 604_800_000 -> SimpleDateFormat("EEE", Locale.getDefault()).format(date)
            else -> SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(date)
        }
    } catch (e: Exception) { "" }
}
