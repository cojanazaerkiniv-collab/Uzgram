package com.uzgram.messenger.features.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSettingsScreen(onBack: () -> Unit) {
    var privateMessages by remember { mutableStateOf(true) }
    var groupMessages by remember { mutableStateOf(true) }
    var channelMessages by remember { mutableStateOf(true) }
    var mentions by remember { mutableStateOf(true) }
    var storyReplies by remember { mutableStateOf(true) }
    var calls by remember { mutableStateOf(true) }
    var botMessages by remember { mutableStateOf(false) }
    var showPreviews by remember { mutableStateOf(true) }
    var vibrate by remember { mutableStateOf(true) }
    var sound by remember { mutableStateOf(true) }
    var badgeCount by remember { mutableStateOf(true) }
    var inAppSounds by remember { mutableStateOf(true) }
    var inAppVibration by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bildirishnomalar", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            NotifSectionLabel("Bildirishnomalar turi")
            NotifCard {
                NotifSwitchItem(
                    icon = Icons.Outlined.Chat,
                    title = "Shaxsiy xabarlar",
                    checked = privateMessages,
                    onToggle = { privateMessages = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                NotifSwitchItem(
                    icon = Icons.Outlined.Group,
                    title = "Guruh xabarlari",
                    checked = groupMessages,
                    onToggle = { groupMessages = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                NotifSwitchItem(
                    icon = Icons.Outlined.CampaignOutlined,
                    title = "Kanal xabarlari",
                    checked = channelMessages,
                    onToggle = { channelMessages = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                NotifSwitchItem(
                    icon = Icons.Outlined.AlternateEmail,
                    title = "Eslatmalar (@mention)",
                    checked = mentions,
                    onToggle = { mentions = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                NotifSwitchItem(
                    icon = Icons.Outlined.AutoStories,
                    title = "Hikoya javoblari",
                    checked = storyReplies,
                    onToggle = { storyReplies = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                NotifSwitchItem(
                    icon = Icons.Outlined.Call,
                    title = "Qo'ng'iroqlar",
                    checked = calls,
                    onToggle = { calls = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                NotifSwitchItem(
                    icon = Icons.Outlined.SmartToy,
                    title = "Bot xabarlari",
                    checked = botMessages,
                    onToggle = { botMessages = it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            NotifSectionLabel("Bildirishnoma ko'rinishi")
            NotifCard {
                NotifSwitchItem(
                    icon = Icons.Outlined.Preview,
                    title = "Oldindan ko'rish",
                    subtitle = "Xabar mazmunini ko'rsatish",
                    checked = showPreviews,
                    onToggle = { showPreviews = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                NotifSwitchItem(
                    icon = Icons.Outlined.Badge,
                    title = "Raqam nishoni",
                    subtitle = "Ilova belgisida o'qilmagan son",
                    checked = badgeCount,
                    onToggle = { badgeCount = it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            NotifSectionLabel("Ovoz va tebranish")
            NotifCard {
                NotifSwitchItem(
                    icon = Icons.Outlined.VolumeUp,
                    title = "Bildirishnoma tovushi",
                    checked = sound,
                    onToggle = { sound = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                NotifSwitchItem(
                    icon = Icons.Outlined.Vibration,
                    title = "Tebranish",
                    checked = vibrate,
                    onToggle = { vibrate = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                NotifSwitchItem(
                    icon = Icons.Outlined.MusicNote,
                    title = "Ilova ichida ovoz",
                    checked = inAppSounds,
                    onToggle = { inAppSounds = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                NotifSwitchItem(
                    icon = Icons.Outlined.PhoneAndroid,
                    title = "Ilova ichida tebranish",
                    checked = inAppVibration,
                    onToggle = { inAppVibration = it }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun NotifSectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
private fun NotifCard(content: @Composable ColumnScope.() -> Unit) {
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
private fun NotifSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!checked) }
            .padding(horizontal = 16.dp, vertical = if (subtitle != null) 10.dp else 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp)
            subtitle?.let { Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}
