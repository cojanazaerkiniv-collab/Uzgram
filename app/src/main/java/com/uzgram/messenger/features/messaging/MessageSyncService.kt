package com.uzgram.messenger.features.messaging

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.uzgram.messenger.data.local.prefs.SessionPrefs
import com.uzgram.messenger.data.remote.websocket.SocketEvent
import com.uzgram.messenger.data.remote.websocket.SocketManager
import com.uzgram.messenger.domain.repository.ChatRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class MessageSyncService : Service() {

    @Inject lateinit var socketManager: SocketManager
    @Inject lateinit var sessionPrefs: SessionPrefs
    @Inject lateinit var chatRepository: ChatRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        scope.launch {
            val token = sessionPrefs.accessToken.first()
            if (token != null && !socketManager.isConnected) {
                socketManager.connect(token)
            }
        }
        observeSocket()
    }

    private fun observeSocket() {
        socketManager.events.onEach { event ->
            when (event) {
                is SocketEvent.Disconnected -> {
                    // Attempt reconnect after delay
                    delay(5000)
                    val token = sessionPrefs.accessToken.first()
                    if (token != null) socketManager.connect(token)
                }
                is SocketEvent.NewMessage -> {
                    // Message is handled by ChatRepositoryImpl socket observer
                }
                else -> {}
            }
        }.launchIn(scope)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
}
