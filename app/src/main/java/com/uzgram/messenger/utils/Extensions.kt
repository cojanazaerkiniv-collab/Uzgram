package com.uzgram.messenger.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.format.Formatter
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// ── Date / time ───────────────────────────────────────────────────────────────

fun String.toReadableTime(): String = try {
    val sdf  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val date = sdf.parse(take(19)) ?: return this
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
} catch (e: Exception) { this }

fun String.toRelativeTime(): String = try {
    val sdf  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val date = sdf.parse(take(19)) ?: return this
    val diff = System.currentTimeMillis() - date.time

    when {
        diff < TimeUnit.MINUTES.toMillis(1)  -> "just now"
        diff < TimeUnit.HOURS.toMillis(1)    -> "${diff / 60_000}m ago"
        diff < TimeUnit.DAYS.toMillis(1)     -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        diff < TimeUnit.DAYS.toMillis(7)     -> SimpleDateFormat("EEE HH:mm", Locale.getDefault()).format(date)
        else                                 -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
    }
} catch (e: Exception) { this }

fun String.toChatListTime(): String = try {
    val sdf  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val date = sdf.parse(take(19)) ?: return this
    val now  = Date()
    val diff = now.time - date.time
    when {
        diff < TimeUnit.MINUTES.toMillis(1)  -> "now"
        diff < TimeUnit.DAYS.toMillis(1)     -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        diff < TimeUnit.DAYS.toMillis(7)     -> SimpleDateFormat("EEE", Locale.getDefault()).format(date)
        else                                 -> SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(date)
    }
} catch (e: Exception) { this }

// ── File size ─────────────────────────────────────────────────────────────────

fun Long.toReadableFileSize(context: Context): String =
    Formatter.formatShortFileSize(context, this)

fun Long.toReadableFileSizeString(): String {
    val kb = this / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1 -> "%.1f GB".format(gb)
        mb >= 1 -> "%.1f MB".format(mb)
        kb >= 1 -> "%.0f KB".format(kb)
        else    -> "$this B"
    }
}

// ── Colors ────────────────────────────────────────────────────────────────────

fun String.toAvatarColor(): Color {
    val colors = listOf(
        Color(0xFF2AABEE), Color(0xFF4CAF50), Color(0xFFFF9500),
        Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF00BCD4),
        Color(0xFFFF5722), Color(0xFF607D8B), Color(0xFF795548),
        Color(0xFF009688)
    )
    return colors[hashCode().and(0x7FFFFFFF) % colors.size]
}

// ── String ────────────────────────────────────────────────────────────────────

fun String.initials(maxChars: Int = 2): String =
    split(" ").take(maxChars).joinToString("") { it.take(1).uppercase() }

fun String.isValidEmail(): Boolean =
    matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+\$"))

fun String.isValidUsername(): Boolean =
    matches(Regex("^[a-zA-Z0-9_]{5,32}\$"))

fun String.isStrongPassword(): Boolean =
    length >= 8 && any { it.isUpperCase() } && any { it.isLowerCase() } && any { it.isDigit() }

// ── Intent helpers ────────────────────────────────────────────────────────────

fun Context.openUrl(url: String) {
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

fun Context.shareText(text: String, title: String = "Share via") {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(intent, title))
}
