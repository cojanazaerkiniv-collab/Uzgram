package com.uzgram.messenger.features.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.uzgram.messenger.data.remote.websocket.SocketManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CallActionReceiver : BroadcastReceiver() {

    @Inject lateinit var socketManager: SocketManager

    override fun onReceive(context: Context, intent: Intent) {
        val callId = intent.getStringExtra("callId") ?: return

        when (intent.action) {
            ACTION_ACCEPT -> {
                // Launch call activity
            }
            ACTION_DECLINE -> {
                socketManager.endCall(callId)
                val manager = context.getSystemService(NotificationManager::class.java)
                manager.cancel(UzGramFirebaseService.CALL_NOTIFICATION_ID)
            }
        }
    }

    companion object {
        const val ACTION_ACCEPT = "com.uzgram.messenger.CALL_ACCEPT"
        const val ACTION_DECLINE = "com.uzgram.messenger.CALL_DECLINE"
    }
}
