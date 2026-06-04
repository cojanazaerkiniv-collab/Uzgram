package com.uzgram.messenger.features.settings

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.uzgram.messenger.R
import com.uzgram.messenger.ui.components.AvatarImage
import com.uzgram.messenger.ui.theme.UzBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onPrivacySecurity: () -> Unit,
    onNotifications: () -> Unit,
    onAppearance: () -> Unit,
    onLanguage: () -> Unit,
    onDataStorage: () -> Unit,
    onDevices: () -> Unit,
    onPremium: () -> Unit,
    onHelp: () -> Unit,
    onAbout: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
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
            // Profile card
            state.user?.let { user ->
                Card(
                    onClick = onEditProfile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarImage(
                            url = user.avatarUrl,
                            fallbackName = user.displayName,
                            size = 64.dp,
                            modifier = Modifier.clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.displayName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            if (!user.username.isNullOrBlank()) {
                                Text("@${user.username}", color = UzBlue, fontSize = 14.sp)
                            }
                            Text(
                                user.email ?: "",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Premium banner
            Card(
                onClick = onPremium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A237E)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.premium_title), color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Imtiyozli funksiyalarni oching", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Settings sections
            SettingsSection(title = "Hisob") {
                SettingsItem(
                    icon = Icons.Outlined.Notifications,
                    iconColor = Color(0xFF5C6BC0),
                    title = stringResource(R.string.notifications_settings),
                    onClick = onNotifications
                )
                SettingsItem(
                    icon = Icons.Outlined.Lock,
                    iconColor = Color(0xFF26A69A),
                    title = stringResource(R.string.privacy_security),
                    onClick = onPrivacySecurity
                )
                SettingsItem(
                    icon = Icons.Outlined.Devices,
                    iconColor = Color(0xFF42A5F5),
                    title = stringResource(R.string.devices),
                    onClick = onDevices
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            SettingsSection(title = "Ilova") {
                SettingsItem(
                    icon = Icons.Outlined.Palette,
                    iconColor = Color(0xFFAB47BC),
                    title = stringResource(R.string.appearance),
                    onClick = onAppearance
                )
                SettingsItem(
                    icon = Icons.Outlined.Language,
                    iconColor = Color(0xFFEF5350),
                    title = stringResource(R.string.language),
                    subtitle = "O'zbek",
                    onClick = onLanguage
                )
                SettingsItem(
                    icon = Icons.Outlined.Storage,
                    iconColor = Color(0xFF66BB6A),
                    title = stringResource(R.string.data_storage),
                    onClick = onDataStorage
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            SettingsSection(title = "Qo'llab-quvvatlash") {
                SettingsItem(
                    icon = Icons.Outlined.HelpOutline,
                    iconColor = Color(0xFFFF7043),
                    title = stringResource(R.string.help_support),
                    onClick = onHelp
                )
                SettingsItem(
                    icon = Icons.Outlined.Info,
                    iconColor = Color(0xFF8D6E63),
                    title = stringResource(R.string.about),
                    subtitle = "v1.0.0",
                    onClick = onAbout
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sign out
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                )
            ) {
                SettingsItem(
                    icon = Icons.Outlined.Logout,
                    iconColor = MaterialTheme.colorScheme.error,
                    title = stringResource(R.string.sign_out),
                    titleColor = MaterialTheme.colorScheme.error,
                    onClick = onSignOut
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (state.showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSignOut() },
            title = { Text("Chiqish") },
            text = { Text(stringResource(R.string.sign_out_confirm)) },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmSignOut() }) {
                    Text(stringResource(R.string.sign_out), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissSignOut() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column { content() }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, color = titleColor)
            subtitle?.let { Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
    }
}
