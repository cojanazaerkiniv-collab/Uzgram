package com.uzgram.messenger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.uzgram.messenger.ui.theme.UzBlue

private val AVATAR_COLORS = listOf(
    Color(0xFF5C6BC0), Color(0xFF26A69A), Color(0xFFEF5350),
    Color(0xFFAB47BC), Color(0xFF42A5F5), Color(0xFFFF7043),
    Color(0xFF66BB6A), Color(0xFFEC407A), Color(0xFF8D6E63),
    Color(0xFF26C6DA)
)

/**
 * Reusable avatar component that shows the user's profile picture
 * or falls back to a colored circle with initials.
 */
@Composable
fun AvatarImage(
    url: String?,
    fallbackName: String = "",
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    if (!url.isNullOrBlank()) {
        AsyncImage(
            model = url,
            contentDescription = fallbackName,
            modifier = modifier
                .size(size)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        val initials = fallbackName
            .split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifBlank { "?" }

        val colorIndex = (fallbackName.hashCode() and 0x7FFFFFFF) % AVATAR_COLORS.size
        val bgColor = AVATAR_COLORS[colorIndex]
        val textSizeSp = (size.value * 0.38f).sp

        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontSize = textSizeSp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
