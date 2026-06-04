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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzgram.messenger.ui.theme.UzBlue

enum class AppTheme { SYSTEM, LIGHT, DARK, AMOLED }
enum class FontSizeOption(val label: String, val scaleFactor: Float) {
    SMALL("Kichik", 0.85f),
    NORMAL("Normal", 1.0f),
    LARGE("Katta", 1.15f),
    EXTRA_LARGE("Juda katta", 1.3f)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(onBack: () -> Unit) {
    var selectedTheme by remember { mutableStateOf(AppTheme.SYSTEM) }
    var selectedFontSize by remember { mutableStateOf(FontSizeOption.NORMAL) }
    var bubbleStyle by remember { mutableIntStateOf(0) }
    var useSystemFont by remember { mutableStateOf(false) }
    var showTimestamps by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ko'rinish", fontWeight = FontWeight.Bold) },
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
            // Theme selection
            SectionLabel("Mavzu")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf(
                        AppTheme.SYSTEM to ("Tizim" to Icons.Outlined.AutoMode),
                        AppTheme.LIGHT to ("Yorug'" to Icons.Outlined.LightMode),
                        AppTheme.DARK to ("Qorong'u" to Icons.Outlined.DarkMode),
                        AppTheme.AMOLED to ("AMOLED" to Icons.Filled.Brightness1)
                    ).forEach { (theme, pair) ->
                        val (label, icon) = pair
                        val isSelected = selectedTheme == theme
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) UzBlue.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surface
                                )
                                .border(
                                    if (isSelected) BorderStroke(2.dp, UzBlue) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedTheme = theme }
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = if (isSelected) UzBlue else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                label,
                                fontSize = 11.sp,
                                color = if (isSelected) UzBlue else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            SectionLabel("Shrift o'lchami")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Preview
                    Text(
                        text = "Salom! Bu namuna matn.",
                        fontSize = (15 * selectedFontSize.scaleFactor).sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("A", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Slider(
                            value = FontSizeOption.entries.indexOf(selectedFontSize).toFloat(),
                            onValueChange = { idx ->
                                selectedFontSize = FontSizeOption.entries[idx.toInt().coerceIn(0, FontSizeOption.entries.lastIndex)]
                            },
                            valueRange = 0f..(FontSizeOption.entries.size - 1).toFloat(),
                            steps = FontSizeOption.entries.size - 2,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        )
                        Text("A", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        selectedFontSize.label,
                        fontSize = 12.sp,
                        color = UzBlue,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            SectionLabel("Xabar pufakchalari uslubi")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("Zamonaviy", "Klassik", "Minimal").forEachIndexed { idx, label ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                if (bubbleStyle == idx) BorderStroke(2.dp, UzBlue) else BorderStroke(0.dp, Color.Transparent),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { bubbleStyle = idx },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, fontSize = 12.sp, fontWeight = if (bubbleStyle == idx) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            SectionLabel("Qo'shimcha")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tizim shriftidan foydalanish", modifier = Modifier.weight(1f))
                        Switch(checked = useSystemFont, onCheckedChange = { useSystemFont = it })
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Vaqt ko'rsatish", modifier = Modifier.weight(1f))
                        Switch(checked = showTimestamps, onCheckedChange = { showTimestamps = it })
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}
