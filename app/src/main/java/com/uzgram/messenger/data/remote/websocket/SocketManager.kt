package com.uzgram.messenger.data.remote.websocket

import com.uzgram.messenger.BuildConfig
import com.uzgram.messenger.domain.model.Message
import com.uzgram.messenger.domain.model.MessageStatus
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketManager @Inject constructor() {

    private var socket: Socket? = null

    private val _events = MutableSharedFlow<SocketEvent>(extraBufferCapacity = 100)
    val events: SharedFlow<SocketEvent> = _events.asSharedFlow()

    fun connect(token: String) {
        try {
            val options = IO.Options.builder()
                .setAuth(mapOf("token" to token))
                .setReconnection(true)
                .setReconnectionAttempts(Int.MAX_VALUE)
                .setReconnectionDelay(1000)
                .setReconnectionDelayMax(30000)
                .setTimeout(20000)
                .build()

            socket = IO.socket(BuildConfig.WS_URL, options)

            socket?.apply {
                on(Socket.EVENT_CONNECT) {
                    _events.tryEmit(SocketEvent.Connected)
                }

                on(Socket.EVENT_DISCONNECT) { args ->
                    val reason = args.firstOrNull()?.toString() ?: "unknown"
                    _events.tryEmit(SocketEvent.Disconnected(reason))
                }

                on(Socket.EVENT_CONNECT_ERROR) { args ->
                    val error = args.firstOrNull()?.toString() ?: "Connection error"
                    _events.tryEmit(SocketEvent.Error(error))
                }

                on("message:new") { args ->
                    args.firstOrNull()?.let { data ->
                        parseMessage(data.toString())?.let { msg ->
                            _events.tryEmit(SocketEvent.NewMessage(msg))
                        }
                    }
                }

                on("message:edited") { args ->
                    args.firstOrNull()?.let { data ->
                        val obj = JSONObject(data.toString())
                        _events.tryEmit(
                            SocketEvent.MessageEdited(
                                messageId = obj.getString("messageId"),
                                content = obj.optString("content"),
                                editedAt = obj.optString("editedAt")
                            )
                        )
                    }
                }

                on("message:deleted") { args ->
                    args.firstOrNull()?.let { data ->
                        val obj = JSONObject(data.toString())
                        _events.tryEmit(
                            SocketEvent.MessageDeleted(
                                messageId = obj.getString("messageId"),
                                chatId = obj.getString("chatId")
                            )
                        )
                    }
                }

                on("message:read:update") { args ->
                    args.firstOrNull()?.let { data ->
                        val obj = JSONObject(data.toString())
                        _events.tryEmit(
                            SocketEvent.MessageRead(
                                messageId = obj.getString("messageId"),
                                userId = obj.getString("userId"),
                                timestamp = obj.getString("timestamp")
                            )
                        )
                    }
                }

                on("typing:indicator") { args ->
                    args.firstOrNull()?.let { data ->
                        val obj = JSONObject(data.toString())
                        _events.tryEmit(
                            SocketEvent.TypingIndicator(
                                chatId = obj.getString("chatId"),
                                userId = obj.getString("userId"),
                                isTyping = obj.getBoolean("isTyping")
                            )
                        )
                    }
                }

                on("user:online") { args ->
                    args.firstOrNull()?.let { data ->
                        val obj = JSONObject(data.toString())
                        _events.tryEmit(SocketEvent.UserOnline(obj.getString("userId")))
                    }
                }

                on("user:offline") { args ->
                    args.firstOrNull()?.let { data ->
                        val obj = JSONObject(data.toString())
                        _events.tryEmit(
                            SocketEvent.UserOffline(
                                userId = obj.getString("userId"),
                                lastSeen = obj.optString("lastSeen")
                            )
                        )
                    }
                }

                on("call:incoming") { args ->
                    args.firstOrNull()?.let { data ->
                        val obj = JSONObject(data.toString())
                        _events.tryEmit(
                            SocketEvent.IncomingCall(
                                callId = obj.getString("callId"),
                                fromUserId = obj.getString("fromUserId"),
                                type = obj.getString("type")
                            )
                        )
                    }
                }

                on("call:accepted") { args ->
                    args.firstOrNull()?.let { data ->
                        val obj = JSONObject(data.toString())
                        _events.tryEmit(
                            SocketEvent.CallAccepted(
                                callId = obj.getString("callId"),
                                sdp = obj.optString("sdp")
                            )
                        )
                    }
                }

                on("call:rejected") { args ->
                    args.firstOrNull()?.let { data ->
                        val obj = JSONObject(data.toString())
                        _events.tryEmit(SocketEvent.CallRejected(obj.getString("callId")))
                    }
                }

                on("call:ended") { args ->
                    args.firstOrNull()?.let { data ->
                        val obj = JSONObject(data.toString())
                        _events.tryEmit(SocketEvent.CallEnded(obj.getString("callId")))
                        _events.tryEmit(SocketEvent.CallStateChanged(
                            callId = obj.getString("callId"),
                            state = "ended"
                        ))
                    }
                }

                on("call:state") { args ->
                    args.firstOrNull()?.let { data ->
                        val obj = JSONObject(data.toString())
                        _events.tryEmit(SocketEvent.CallStateChanged(
                            callId = obj.getString("callId"),
                            state = obj.getString("state")
                        ))
                    }
                }

                on("call:accepted") { args ->
                    args.firstOrNull()?.let { data ->
                        val obj = JSONObject(data.toString())
                        _events.tryEmit(SocketEvent.CallStateChanged(
                            callId = obj.getString("callId"),
                            state = "connected"
                        ))
                    }
                }

                on("call:candidate") { args ->
                    args.firstOrNull()?.let { data ->
                        val obj = JSONObject(data.toString())
                        _events.tryEmit(
                            SocketEvent.IceCandidate(
                                callId = obj.getString("callId"),
                                candidate = obj.getJSONObject("candidate").toString()
                            )
                        )
                    }
                }

                connect()
            }
        } catch (e: Exception) {
            _events.tryEmit(SocketEvent.Error("Failed to initialize socket: ${e.message}"))
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
    }

    fun sendTypingStart(chatId: String) {
        socket?.emit("typing:start", JSONObject().put("chatId", chatId))
    }

    fun sendTypingStop(chatId: String) {
        socket?.emit("typing:stop", JSONObject().put("chatId", chatId))
    }

    fun sendCallOffer(userId: String, sdp: String, type: String) {
        socket?.emit("call:offer", JSONObject()
            .put("userId", userId)
            .put("sdp", sdp)
            .put("type", type))
    }

    fun sendCallAnswer(callId: String, sdp: String) {
        socket?.emit("call:answer", JSONObject()
            .put("callId", callId)
            .put("sdp", sdp))
    }

    fun sendIceCandidate(callId: String, candidate: String) {
        socket?.emit("call:candidate", JSONObject()
            .put("callId", callId)
            .put("candidate", JSONObject(candidate)))
    }

    fun endCall(callId: String) {
        socket?.emit("call:end", JSONObject().put("callId", callId))
    }

    fun sendMuteState(muted: Boolean) {
        socket?.emit("call:mute", JSONObject().put("muted", muted))
    }

    val isConnected: Boolean get() = socket?.connected() == true

    private fun parseMessage(json: String): Message? {
        return try {
            val obj = JSONObject(json)
            Message(
                id = obj.getString("id"),
                chatId = obj.getString("chatId"),
                senderId = obj.getString("senderId"),
                senderName = obj.getString("senderName"),
                senderUsername = obj.getString("senderUsername"),
                senderAvatarUrl = obj.optString("senderAvatarUrl").takeIf { it.isNotEmpty() },
                type = com.uzgram.messenger.domain.model.MessageType.valueOf(
                    obj.optString("type", "text").uppercase()
                ),
                text = obj.optString("text").takeIf { it.isNotEmpty() },
                mediaUrl = obj.optString("mediaUrl").takeIf { it.isNotEmpty() },
                replyToId = obj.optString("replyToId").takeIf { it.isNotEmpty() },
                status = MessageStatus.SENT,
                createdAt = obj.optString("createdAt"),
                updatedAt = obj.optString("updatedAt")
            )
        } catch (e: Exception) {
            null
        }
    }
}

sealed class SocketEvent {
    object Connected : SocketEvent()
    data class Disconnected(val reason: String) : SocketEvent()
    data class Error(val message: String) : SocketEvent()
    data class NewMessage(val message: Message) : SocketEvent()
    data class MessageEdited(val messageId: String, val content: String, val editedAt: String) : SocketEvent()
    data class MessageDeleted(val messageId: String, val chatId: String) : SocketEvent()
    data class MessageRead(val messageId: String, val userId: String, val timestamp: String) : SocketEvent()
    data class TypingIndicator(val chatId: String, val userId: String, val isTyping: Boolean) : SocketEvent()
    data class UserOnline(val userId: String) : SocketEvent()
    data class UserOffline(val userId: String, val lastSeen: String) : SocketEvent()
    data class IncomingCall(val callId: String, val fromUserId: String, val type: String) : SocketEvent()
    data class CallAccepted(val callId: String, val sdp: String) : SocketEvent()
    data class CallRejected(val callId: String) : SocketEvent()
    data class CallEnded(val callId: String) : SocketEvent()
    data class IceCandidate(val callId: String, val candidate: String) : SocketEvent()
    data class CallStateChanged(val callId: String, val state: String) : SocketEvent()
}
