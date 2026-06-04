package com.uzgram.messenger.features.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.uzgram.messenger.domain.model.User
import com.uzgram.messenger.ui.components.AvatarImage
import com.uzgram.messenger.ui.theme.UzBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onSettings: () -> Unit,
    onEditProfile: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Profil", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Sozlamalar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            // Profile header
            state.user?.let { user ->
                ProfileHeader(
                    user = user,
                    onEditProfile = onEditProfile,
                    onAvatarClick = { viewModel.showAvatarOptions() }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stats row
            state.user?.let { user ->
                ProfileStatsRow(
                    postsCount = state.postsCount,
                    storiesCount = state.storiesCount,
                    contactsCount = state.contactsCount
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            state.user?.let { user ->
                ProfileActionButtons(
                    onMessage = { /* navigate to chat */ },
                    onVoiceCall = { /* start voice call */ },
                    onVideoCall = { /* start video call */ }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Shared media section placeholder
            ProfileSectionCard(
                title = "Media, fayllar va havolalar",
                icon = Icons.Outlined.PhotoLibrary,
                onClick = { /* navigate to media */ }
            )
            ProfileSectionCard(
                title = "Yulduzchali xabarlar",
                icon = Icons.Outlined.Star,
                onClick = { /* navigate to starred */ }
            )
            ProfileSectionCard(
                title = "Umumiy guruhlar",
                icon = Icons.Outlined.Group,
                onClick = { /* navigate to groups */ }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Dialogs
    if (state.showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSignOut() },
            title = { Text("Chiqish") },
            text = { Text("Hisobingizdan chiqmoqchimisiz?") },
            confirmButton = {
                TextButton(onClick = { viewModel.signOut() }) {
                    Text("Chiqish", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissSignOut() }) {
                    Text("Bekor qilish")
                }
            }
        )
    }
}

@Composable
private fun ProfileHeader(
    user: User,
    onEditProfile: () -> Unit,
    onAvatarClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clickable(onClick = onAvatarClick)
            ) {
                AvatarImage(
                    url = user.avatarUrl,
                    fallbackName = user.displayName,
                    size = 88.dp,
                    modifier = Modifier
                        .clip(CircleShape)
                        .border(2.dp, UzBlue, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .align(Alignment.BottomEnd)
                        .background(UzBlue, CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = user.displayName,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )

            if (!user.username.isNullOrBlank()) {
                Text(
                    text = "@${user.username}",
                    color = UzBlue,
                    fontSize = 14.sp
                )
            }

            if (!user.bio.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = user.bio,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        }

        // Edit button
        IconButton(
            onClick = onEditProfile,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(Icons.Outlined.EditNote, contentDescription = "Profilni tahrirlash")
        }
    }
}

@Composable
private fun ProfileStatsRow(postsCount: Int, storiesCount: Int, contactsCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(value = postsCount.toString(), label = "Postlar")
        VerticalDivider(modifier = Modifier.height(40.dp))
        StatItem(value = storiesCount.toString(), label = "Hikoyalar")
        VerticalDivider(modifier = Modifier.height(40.dp))
        StatItem(value = contactsCount.toString(), label = "Kontaktlar")
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
    }
}

@Composable
private fun ProfileActionButtons(
    onMessage: () -> Unit,
    onVoiceCall: () -> Unit,
    onVideoCall: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onMessage,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Outlined.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Xabar")
        }
        OutlinedButton(
            onClick = onVoiceCall,
            modifier = Modifier.weight(0.5f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Outlined.Call, contentDescription = null, modifier = Modifier.size(18.dp))
        }
        OutlinedButton(
            onClick = onVideoCall,
            modifier = Modifier.weight(0.5f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Outlined.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun ProfileSectionCard(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = UzBlue, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Text(title, modifier = Modifier.weight(1f), fontSize = 15.sp)
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
