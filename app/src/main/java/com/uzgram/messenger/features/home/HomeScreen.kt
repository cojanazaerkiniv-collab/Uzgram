package com.uzgram.messenger.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.uzgram.messenger.R
import com.uzgram.messenger.domain.model.StoryGroup
import com.uzgram.messenger.domain.model.User
import com.uzgram.messenger.ui.theme.UzBlue
import com.uzgram.messenger.ui.theme.UzBlueDeep
import com.uzgram.messenger.ui.components.AvatarImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStoryClick: (userId: String, storyIndex: Int) -> Unit,
    onAddStory: () -> Unit,
    onChatClick: (chatId: String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "UzGram",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = UzBlue
                    )
                },
                actions = {
                    IconButton(onClick = { /* QR Scanner */ }) {
                        Icon(Icons.Outlined.QrCodeScanner, contentDescription = null)
                    }
                    IconButton(onClick = { /* New message */ }) {
                        Icon(Icons.Outlined.Edit, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Stories row
            item {
                StoriesRow(
                    storyGroups = state.storyGroups,
                    currentUser = state.currentUser,
                    onAddStory = onAddStory,
                    onStoryClick = onStoryClick
                )
                HorizontalDivider(thickness = 0.5.dp)
            }

            // Online contacts row
            if (state.onlineContacts.isNotEmpty()) {
                item {
                    OnlineContactsRow(
                        contacts = state.onlineContacts,
                        onContactClick = { user -> onChatClick(user.id) }
                    )
                    HorizontalDivider(thickness = 0.5.dp)
                }
            }

            // Recent activity / pinned news feed placeholder
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Empty state if nothing to show
            if (state.storyGroups.isEmpty() && state.onlineContacts.isEmpty()) {
                item {
                    EmptyHomeState(onAddStory = onAddStory)
                }
            }
        }
    }
}

@Composable
private fun StoriesRow(
    storyGroups: List<StoryGroup>,
    currentUser: User?,
    onAddStory: () -> Unit,
    onStoryClick: (userId: String, storyIndex: Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.stories_title),
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            TextButton(onClick = { /* Show all stories */ }) {
                Text(text = "Barchasi", color = UzBlue)
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Add story button
            item {
                AddStoryItem(
                    avatarUrl = currentUser?.avatarUrl,
                    onClick = onAddStory
                )
            }

            // Story groups
            items(storyGroups, key = { it.userId }) { group ->
                StoryAvatarItem(
                    group = group,
                    onClick = { onStoryClick(group.userId, 0) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun AddStoryItem(avatarUrl: String?, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.size(64.dp)) {
            AvatarImage(
                url = avatarUrl,
                size = 64.dp,
                modifier = Modifier.clip(CircleShape)
            )
            // Plus badge
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .align(Alignment.BottomEnd)
                    .background(UzBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.your_story),
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StoryAvatarItem(group: StoryGroup, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick)
    ) {
        // Gradient ring for unread stories
        val ringBrush = if (group.unreadCount > 0) {
            Brush.linearGradient(colors = listOf(UzBlueDeep, UzBlue, Color(0xFF00C6FF)))
        } else {
            Brush.linearGradient(colors = listOf(Color.Gray, Color.Gray))
        }

        Box(
            modifier = Modifier
                .size(64.dp)
                .background(ringBrush, CircleShape)
                .padding(2.5.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .padding(2.dp)
        ) {
            AvatarImage(
                url = group.userAvatarUrl,
                fallbackName = group.userName,
                size = 60.dp,
                modifier = Modifier.clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = group.userName.split(" ").first(),
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun OnlineContactsRow(
    contacts: List<User>,
    onContactClick: (User) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.online_contacts),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(contacts, key = { it.id }) { user ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(56.dp)
                        .clickable { onContactClick(user) }
                ) {
                    Box(modifier = Modifier.size(52.dp)) {
                        AvatarImage(
                            url = user.avatarUrl,
                            fallbackName = user.displayName,
                            size = 52.dp,
                            modifier = Modifier.clip(CircleShape)
                        )
                        // Online dot
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .align(Alignment.BottomEnd)
                                .background(Color(0xFF4CAF50), CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f), CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user.displayName.split(" ").first(),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun EmptyHomeState(onAddStory: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Outlined.AutoStories,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Hikoyalaringizni ulashing",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Do'stlaringiz bilan suratlar va videolar ulashing",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onAddStory) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.create_story))
        }
    }
}
