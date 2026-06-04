package com.uzgram.messenger

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class UzGramApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val channels = listOf(
                NotificationChannel(
                    CHANNEL_MESSAGES,
                    "Messages",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "New message notifications"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_CALLS,
                    "Calls",
                    NotificationManager.IMPORTANCE_MAX
                ).apply {
                    description = "Incoming call notifications"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_GROUP_MESSAGES,
                    "Group Messages",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Group message notifications"
                },
                NotificationChannel(
                    CHANNEL_MENTIONS,
                    "Mentions",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Mention notifications"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_STORIES,
                    "Stories",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Story notifications"
                },
                NotificationChannel(
                    CHANNEL_SYSTEM,
                    "System",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "System notifications"
                }
            )

            channels.forEach { manager.createNotificationChannel(it) }
        }
    }

    companion object {
        const val CHANNEL_MESSAGES = "uzgram_messages"
        const val CHANNEL_CALLS = "uzgram_calls"
        const val CHANNEL_GROUP_MESSAGES = "uzgram_group_messages"
        const val CHANNEL_MENTIONS = "uzgram_mentions"
        const val CHANNEL_STORIES = "uzgram_stories"
        const val CHANNEL_SYSTEM = "uzgram_system"
    }
}
