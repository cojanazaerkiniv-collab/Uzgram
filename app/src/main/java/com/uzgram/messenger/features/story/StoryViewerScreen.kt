package com.uzgram.messenger.features.story

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.uzgram.messenger.domain.model.Story
import com.uzgram.messenger.domain.model.StoryMediaType
import com.uzgram.messenger.ui.components.AvatarImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val STORY_DURATION_MS = 5000L

@Composable
fun StoryViewerScreen(
    userId: String,
    initialIndex: Int = 0,
    onClose: () -> Unit,
    viewModel: StoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        viewModel.loadStoriesForUser(userId)
    }

    val stories = state.currentGroupStories
    var currentIndex by remember(initialIndex) { mutableIntStateOf(initialIndex) }
    var progressFraction by remember { mutableFloatStateOf(0f) }
    var isPaused by remember { mutableStateOf(false) }

    // Auto-advance timer
    LaunchedEffect(currentIndex, isPaused) {
        if (isPaused) return@LaunchedEffect
        val story = stories.getOrNull(currentIndex) ?: return@LaunchedEffect
        val duration = if (story.mediaType == StoryMediaType.VIDEO) 15_000L else STORY_DURATION_MS
        val step = duration / 100
        repeat(100) { i ->
            if (!isPaused) {
                progressFraction = (i + 1) / 100f
                delay(step)
            }
        }
        // Advance to next or close
        if (currentIndex < stories.lastIndex) {
            currentIndex++
            progressFraction = 0f
        } else {
            onClose()
        }
    }

    val currentStory = stories.getOrNull(currentIndex)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { isPaused = true },
                    onPress = {
                        val x = it.x
                        val width = size.width
                        tryAwaitRelease()
                        isPaused = false
                        if (x < width / 3f) {
                            // Previous
                            if (currentIndex > 0) { currentIndex--; progressFraction = 0f }
                        } else if (x > width * 2 / 3f) {
                            // Next
                            if (currentIndex < stories.lastIndex) { currentIndex++; progressFraction = 0f }
                            else onClose()
                        }
                    }
                )
            }
    ) {
        // Story media
        currentStory?.let { story ->
            StoryMediaContent(story = story)
        }

        // Top gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
                    )
                )
        )

        // Bottom gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                    )
                )
        )

        // Progress bars
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            stories.forEachIndexed { index, _ ->
                val fraction = when {
                    index < currentIndex -> 1f
                    index == currentIndex -> progressFraction
                    else -> 0f
                }
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.35f)
                )
            }
        }

        // Header: avatar + name + time + close
        currentStory?.let { story ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarImage(
                    url = story.authorAvatarUrl,
                    fallbackName = story.authorName,
                    size = 38.dp,
                    modifier = Modifier
                        .clip(CircleShape)
                        .border(1.5.dp, Color.White, CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(story.authorName, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(formatTimeAgo(story.createdAt.epochSecond), color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
                if (story.isOwn) {
                    IconButton(onClick = { viewModel.showViewers(story.id) }) {
                        Icon(Icons.Outlined.Visibility, contentDescription = "Ko'rganlar", tint = Color.White)
                    }
                    IconButton(onClick = { viewModel.deleteStory(story.id, onClose) }) {
                        Icon(Icons.Outlined.Delete, contentDescription = "O'chirish", tint = Color.White)
                    }
                } else {
                    IconButton(onClick = { /* More options */ }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = null, tint = Color.White)
                    }
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = "Yopish", tint = Color.White)
                }
            }
        }

        // Bottom: reply bar
        currentStory?.let { story ->
            if (!story.isOwn) {
                var replyText by remember { mutableStateOf("") }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(12.dp)
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(28.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextFieldCompat(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = "Javob yuboring…",
                        modifier = Modifier.weight(1f)
                    )
                    if (replyText.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                viewModel.replyToStory(story.id, replyText)
                                replyText = ""
                            }
                        ) {
                            Icon(Icons.Filled.Send, contentDescription = null, tint = Color.White)
                        }
                    } else {
                        Row {
                            listOf("❤️", "😮", "😢", "😂", "🔥").forEach { emoji ->
                                Text(
                                    text = emoji,
                                    fontSize = 22.sp,
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .clickable { viewModel.reactToStory(story.id, emoji) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StoryMediaContent(story: Story) {
    when (story.mediaType) {
        StoryMediaType.IMAGE -> {
            AsyncImage(
                model = story.mediaUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        StoryMediaType.VIDEO -> {
            // Placeholder — integrate ExoPlayer for video playback
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1A1A2E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PlayCircle, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(64.dp))
            }
        }
        StoryMediaType.TEXT -> {
            val bg = if (story.gradientColors.size >= 2) {
                Brush.verticalGradient(story.gradientColors.map { Color(it) })
            } else {
                Brush.linearGradient(listOf(Color(0xFF6A11CB), Color(0xFF2575FC)))
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = story.textContent ?: "",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BasicTextFieldCompat(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    if (value.isEmpty()) {
        Text(text = placeholder, color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp, modifier = modifier)
    }
}

private fun formatTimeAgo(epochSecond: Long): String {
    val diff = System.currentTimeMillis() / 1000 - epochSecond
    return when {
        diff < 60 -> "hozir"
        diff < 3600 -> "${diff / 60} daqiqa oldin"
        diff < 86400 -> "${diff / 3600} soat oldin"
        else -> "${diff / 86400} kun oldin"
    }
}
