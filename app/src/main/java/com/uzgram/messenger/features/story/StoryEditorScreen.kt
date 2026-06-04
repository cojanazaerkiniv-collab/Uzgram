package com.uzgram.messenger.features.story

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.uzgram.messenger.domain.model.CreateStoryRequest
import com.uzgram.messenger.domain.model.StoryMediaType
import com.uzgram.messenger.domain.model.StoryPrivacy
import com.uzgram.messenger.ui.theme.UzBlue

private val GRADIENT_PRESETS = listOf(
    listOf(0xFF6A11CB, 0xFF2575FC),
    listOf(0xFFf7971e, 0xFFffd200),
    listOf(0xFF11998e, 0xFF38ef7d),
    listOf(0xFFFF416C, 0xFFFF4B2B),
    listOf(0xFF1D2671, 0xFFC33764),
    listOf(0xFF3a1c71, 0xFFd76d77, 0xFFffaf7b),
    listOf(0xFF000000, 0xFF434343),
    listOf(0xFFFFFFFF, 0xFFDBDBDB)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryEditorScreen(
    onPublish: () -> Unit,
    onBack: () -> Unit,
    viewModel: StoryViewModel = hiltViewModel()
) {
    val state by viewModel.editorState.collectAsState()

    var textContent by remember { mutableStateOf("") }
    var selectedGradient by remember { mutableIntStateOf(0) }
    var selectedPrivacy by remember { mutableStateOf(StoryPrivacy.EVERYONE) }
    var showPrivacySheet by remember { mutableStateOf(false) }
    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var storyType by remember { mutableStateOf(StoryMediaType.TEXT) }

    val mediaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedMediaUri = it
            storyType = StoryMediaType.IMAGE
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.Close, contentDescription = null, tint = Color.White)
                    }
                },
                title = { Text("Hikoya yaratish", color = Color.White) },
                actions = {
                    // Story type toggle
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            StoryMediaType.TEXT to Icons.Outlined.TextFields,
                            StoryMediaType.IMAGE to Icons.Outlined.Image,
                            StoryMediaType.VIDEO to Icons.Outlined.VideoLibrary
                        ).forEach { (type, icon) ->
                            IconButton(
                                onClick = {
                                    if (type != StoryMediaType.TEXT) {
                                        val mime = if (type == StoryMediaType.VIDEO) "video/*" else "image/*"
                                        mediaLauncher.launch(mime)
                                    } else {
                                        storyType = StoryMediaType.TEXT
                                        selectedMediaUri = null
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(icon, contentDescription = null, tint = if (storyType == type) Color.White else Color.White.copy(alpha = 0.5f))
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Canvas / preview area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
                    .align(Alignment.TopCenter),
                contentAlignment = Alignment.Center
            ) {
                if (selectedMediaUri != null) {
                    AsyncImage(
                        model = selectedMediaUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    val gradient = GRADIENT_PRESETS.getOrElse(selectedGradient) { GRADIENT_PRESETS[0] }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(gradient.map { Color(it) })),
                        contentAlignment = Alignment.Center
                    ) {
                        if (textContent.isNotEmpty()) {
                            Text(
                                text = textContent,
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(24.dp)
                            )
                        } else {
                            Text(
                                text = "Biror narsa yozing…",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 22.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Bottom controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color(0xFF1A1A1A))
                    .padding(16.dp)
            ) {
                // Text input (for text stories)
                if (storyType == StoryMediaType.TEXT) {
                    OutlinedTextField(
                        value = textContent,
                        onValueChange = { textContent = it },
                        placeholder = { Text("Biror narsa yozing…", color = Color.White.copy(alpha = 0.4f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = UzBlue,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            cursorColor = UzBlue
                        ),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Gradient picker
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(GRADIENT_PRESETS.indices.toList()) { idx ->
                            val g = GRADIENT_PRESETS[idx]
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(g.map { Color(it) }))
                                    .clickable { selectedGradient = idx }
                                    .then(
                                        if (selectedGradient == idx)
                                            Modifier.border(2.dp, Color.White, CircleShape)
                                        else Modifier
                                    )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Privacy & Publish row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showPrivacySheet = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Icon(Icons.Outlined.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (selectedPrivacy) {
                                StoryPrivacy.EVERYONE -> "Hamma"
                                StoryPrivacy.CONTACTS -> "Kontaktlar"
                                StoryPrivacy.CLOSE_FRIENDS -> "Yaqin do'stlar"
                                StoryPrivacy.EXCLUDE -> "Chiqarib tashlash"
                            }
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.publishStory(
                                CreateStoryRequest(
                                    mediaUri = selectedMediaUri?.toString(),
                                    mediaType = storyType,
                                    textContent = textContent.ifBlank { null },
                                    backgroundColor = null,
                                    gradientColors = GRADIENT_PRESETS.getOrElse(selectedGradient) { GRADIENT_PRESETS[0] },
                                    privacy = selectedPrivacy,
                                    excludedUserIds = emptyList(),
                                    closeFriendUserIds = emptyList(),
                                    linkUrl = null,
                                    linkTitle = null,
                                    stickers = emptyList()
                                )
                            ) { onPublish() }
                        },
                        enabled = textContent.isNotBlank() || selectedMediaUri != null
                    ) {
                        if (state.isPublishing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text("Nashr qilish")
                        }
                    }
                }
            }
        }
    }

    // Privacy bottom sheet
    if (showPrivacySheet) {
        ModalBottomSheet(onDismissRequest = { showPrivacySheet = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Kimlar ko'ra oladi", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                listOf(
                    StoryPrivacy.EVERYONE to "Hamma",
                    StoryPrivacy.CONTACTS to "Kontaktlarim",
                    StoryPrivacy.CLOSE_FRIENDS to "Yaqin do'stlar"
                ).forEach { (privacy, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedPrivacy = privacy
                                showPrivacySheet = false
                            }
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPrivacy == privacy,
                            onClick = { selectedPrivacy = privacy; showPrivacySheet = false }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(label, fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
