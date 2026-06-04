package com.uzgram.messenger.features.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.uzgram.messenger.MainActivity
import com.uzgram.messenger.R
import com.uzgram.messenger.UzGramApplication.Companion.CHANNEL_CALLS
import com.uzgram.messenger.UzGramApplication.Companion.CHANNEL_GROUP_MESSAGES
import com.uzgram.messenger.UzGramApplication.Companion.CHANNEL_MESSAGES
import com.uzgram.messenger.UzGramApplication.Companion.CHANNEL_MENTIONS
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UzGramFirebaseService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = message.data
        val type = data["type"] ?: "message"
        val title = message.notification?.title ?: data["title"] ?: "UzGram"
        val body = message.notification?.body ?: data["body"] ?: ""
        val chatId = data["chatId"]
        val callId = data["callId"]
        val senderId = data["senderId"]

        when (type) {
            "message" -> showMessageNotification(title, body, chatId)
            "group_message" -> showGroupMessageNotification(title, body, chatId)
            "mention" -> showMentionNotification(title, body, chatId)
            "call" -> showCallNotification(title, body, callId, senderId)
            else -> showMessageNotification(title, body, chatId)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Send token to server
    }

    private fun showMessageNotification(title: String, body: String, chatId: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            chatId?.let { putExtra("chatId", it) }
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_MESSAGES)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(chatId.hashCode(), notification)
    }

    private fun showGroupMessageNotification(title: String, body: String, chatId: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            chatId?.let { putExtra("chatId", it) }
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_GROUP_MESSAGES)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(chatId.hashCode(), notification)
    }

    private fun showMentionNotification(title: String, body: String, chatId: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            chatId?.let { putExtra("chatId", it) }
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_MENTIONS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun showCallNotification(title: String, body: String, callId: String?, senderId: String?) {
        val fullScreenIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("action", "incoming_call")
            callId?.let { putExtra("callId", it) }
            senderId?.let { putExtra("senderId", it) }
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, 1, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val acceptIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = CallActionReceiver.ACTION_ACCEPT
            callId?.let { putExtra("callId", it) }
        }
        val acceptPendingIntent = PendingIntent.getBroadcast(
            this, 2, acceptIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val declineIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = CallActionReceiver.ACTION_DECLINE
            callId?.let { putExtra("callId", it) }
        }
        val declinePendingIntent = PendingIntent.getBroadcast(
            this, 3, declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_CALLS)
            .setSmallIcon(R.drawable.ic_call)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(R.drawable.ic_call_end, "Decline", declinePendingIntent)
            .addAction(R.drawable.ic_call, "Accept", acceptPendingIntent)
            .setAutoCancel(true)
            .setOngoing(true)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(CALL_NOTIFICATION_ID, notification)
    }

    companion object {
        const val CALL_NOTIFICATION_ID = 9001
    }
}
