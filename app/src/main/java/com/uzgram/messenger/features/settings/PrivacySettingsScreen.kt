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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsScreen(onBack: () -> Unit) {
    var lastSeenPrivacy by remember { mutableStateOf("Hamma") }
    var profilePhotoPrivacy by remember { mutableStateOf("Hamma") }
    var bioPrivacy by remember { mutableStateOf("Hamma") }
    var forwardPrivacy by remember { mutableStateOf("Hamma") }
    var callPrivacy by remember { mutableStateOf("Hamma") }
    var groupAddPrivacy by remember { mutableStateOf("Hamma") }
    var storyPrivacy by remember { mutableStateOf("Hamma") }
    var twoStepEnabled by remember { mutableStateOf(false) }
    var blockedUsers by remember { mutableIntStateOf(0) }

    val privacyOptions = listOf("Hamma", "Kontaktlarim", "Hech kim")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Maxfiylik va xavfsizlik", fontWeight = FontWeight.Bold) },
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
            SectionHeader("Maxfiylik")

            PrivacySection {
                PrivacyDropdownItem(
                    icon = Icons.Outlined.Visibility,
                    title = "So'nggi faollik",
                    current = lastSeenPrivacy,
                    options = privacyOptions,
                    onSelect = { lastSeenPrivacy = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                PrivacyDropdownItem(
                    icon = Icons.Outlined.AccountCircle,
                    title = "Profil rasmi",
                    current = profilePhotoPrivacy,
                    options = privacyOptions,
                    onSelect = { profilePhotoPrivacy = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                PrivacyDropdownItem(
                    icon = Icons.Outlined.Notes,
                    title = "Bio",
                    current = bioPrivacy,
                    options = privacyOptions,
                    onSelect = { bioPrivacy = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                PrivacyDropdownItem(
                    icon = Icons.Outlined.ForwardToInbox,
                    title = "Yo'naltirilgan xabarlar",
                    current = forwardPrivacy,
                    options = privacyOptions,
                    onSelect = { forwardPrivacy = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                PrivacyDropdownItem(
                    icon = Icons.Outlined.Call,
                    title = "Qo'ng'iroqlar",
                    current = callPrivacy,
                    options = privacyOptions,
                    onSelect = { callPrivacy = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                PrivacyDropdownItem(
                    icon = Icons.Outlined.Group,
                    title = "Guruhga qo'shish",
                    current = groupAddPrivacy,
                    options = privacyOptions,
                    onSelect = { groupAddPrivacy = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                PrivacyDropdownItem(
                    icon = Icons.Outlined.AutoStories,
                    title = "Hikoyalar",
                    current = storyPrivacy,
                    options = privacyOptions,
                    onSelect = { storyPrivacy = it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader("Xavfsizlik")

            PrivacySection {
                PrivacySwitchItem(
                    icon = Icons.Outlined.Security,
                    title = "Ikki bosqichli tasdiqlash",
                    subtitle = "Hisobni ekstra himoyalash",
                    checked = twoStepEnabled,
                    onToggle = { twoStepEnabled = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                PrivacyNavigationItem(
                    icon = Icons.Outlined.Devices,
                    title = "Faol seanslar",
                    subtitle = "Qurilmalaringizni boshqaring",
                    onClick = {}
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                PrivacyNavigationItem(
                    icon = Icons.Outlined.Block,
                    title = "Bloklangan foydalanuvchilar",
                    subtitle = "$blockedUsers foydalanuvchi",
                    onClick = {}
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                PrivacyNavigationItem(
                    icon = Icons.Outlined.DeleteForever,
                    title = "Hisobni o'chirish",
                    subtitle = "Bu amalni qaytarib bo'lmaydi",
                    onClick = {},
                    dangerColor = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
private fun PrivacySection(content: @Composable ColumnScope.() -> Unit) {
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
private fun PrivacyDropdownItem(
    icon: ImageVector,
    title: String,
    current: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(title, modifier = Modifier.weight(1f))
        Text(current, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = { onSelect(opt); expanded = false },
                    trailingIcon = if (opt == current) {
                        { Icon(Icons.Filled.Check, contentDescription = null) }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun PrivacySwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}

@Composable
private fun PrivacyNavigationItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    dangerColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = dangerColor, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, color = dangerColor)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
