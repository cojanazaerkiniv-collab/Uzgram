package com.uzgram.messenger.features.calls

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.uzgram.messenger.MainActivity
import com.uzgram.messenger.R
import com.uzgram.messenger.UzGramApplication.Companion.CHANNEL_CALLS
import com.uzgram.messenger.data.remote.websocket.SocketEvent
import com.uzgram.messenger.data.remote.websocket.SocketManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.webrtc.*
import javax.inject.Inject

@AndroidEntryPoint
class CallService : Service() {

    @Inject lateinit var socketManager: SocketManager

    private val binder = CallBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null

    private var callId: String? = null
    private var isVideoCall: Boolean = false

    inner class CallBinder : Binder() {
        fun getService(): CallService = this@CallService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        initWebRTC()
        observeSocketEvents()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_CALL -> {
                callId = intent.getStringExtra("callId")
                isVideoCall = intent.getBooleanExtra("isVideo", false)
                startForeground(CALL_NOTIFICATION_ID, buildCallNotification())
            }
            ACTION_END_CALL -> {
                endCall()
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun initWebRTC() {
        val options = PeerConnectionFactory.InitializationOptions.builder(applicationContext)
            .setEnableInternalTracer(false)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        val encoderFactory = DefaultVideoEncoderFactory(
            EglBase.create().eglBaseContext, true, true
        )
        val decoderFactory = DefaultVideoDecoderFactory(EglBase.create().eglBaseContext)

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .createPeerConnectionFactory()
    }

    fun startOutgoingCall(userId: String, isVideo: Boolean) {
        isVideoCall = isVideo
        createLocalTracks()
        createPeerConnection()
        peerConnection?.let { pc ->
            pc.createOffer(object : SdpObserver {
                override fun onCreateSuccess(sdp: SessionDescription) {
                    pc.setLocalDescription(this, sdp)
                    socketManager.sendCallOffer(userId, sdp.description, if (isVideo) "video" else "voice")
                }
                override fun onSetSuccess() {}
                override fun onCreateFailure(error: String) {}
                override fun onSetFailure(error: String) {}
            }, MediaConstraints())
        }
    }

    fun acceptIncomingCall(sdpOffer: String) {
        createLocalTracks()
        createPeerConnection()
        peerConnection?.let { pc ->
            pc.setRemoteDescription(
                object : SdpObserver {
                    override fun onSetSuccess() {
                        pc.createAnswer(object : SdpObserver {
                            override fun onCreateSuccess(sdp: SessionDescription) {
                                pc.setLocalDescription(this, sdp)
                                callId?.let { socketManager.sendCallAnswer(it, sdp.description) }
                            }
                            override fun onSetSuccess() {}
                            override fun onCreateFailure(e: String) {}
                            override fun onSetFailure(e: String) {}
                        }, MediaConstraints())
                    }
                    override fun onCreateSuccess(sdp: SessionDescription) {}
                    override fun onCreateFailure(e: String) {}
                    override fun onSetFailure(e: String) {}
                },
                SessionDescription(SessionDescription.Type.OFFER, sdpOffer)
            )
        }
    }

    fun endCall() {
        callId?.let { socketManager.endCall(it) }
        peerConnection?.close()
        peerConnection = null
        localVideoTrack?.dispose()
        localAudioTrack?.dispose()
    }

    private fun createLocalTracks() {
        val audioSource = peerConnectionFactory?.createAudioSource(MediaConstraints())
        localAudioTrack = peerConnectionFactory?.createAudioTrack("ARDAMSa0", audioSource)

        if (isVideoCall) {
            val videoSource = peerConnectionFactory?.createVideoSource(false)
            localVideoTrack = peerConnectionFactory?.createVideoTrack("ARDAMSv0", videoSource)
        }
    }

    private fun createPeerConnection() {
        val rtcConfig = PeerConnection.RTCConfiguration(
            listOf(
                PeerConnection.IceServer.builder("stun:stun.uzgram.online:3478").createIceServer(),
                PeerConnection.IceServer.builder("turn:turn.uzgram.online:3478")
                    .setUsername("uzgram").setPassword("uzgram_turn_secret")
                    .createIceServer()
            )
        ).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }

        peerConnection = peerConnectionFactory?.createPeerConnection(
            rtcConfig,
            object : PeerConnection.Observer {
                override fun onIceCandidate(candidate: IceCandidate) {
                    callId?.let {
                        socketManager.sendIceCandidate(
                            it,
                            """{"sdpMid":"${candidate.sdpMid}","sdpMLineIndex":${candidate.sdpMLineIndex},"candidate":"${candidate.sdp}"}"""
                        )
                    }
                }
                override fun onSignalingChange(s: PeerConnection.SignalingState) {}
                override fun onIceConnectionChange(s: PeerConnection.IceConnectionState) {}
                override fun onIceConnectionReceivingChange(b: Boolean) {}
                override fun onIceGatheringChange(s: PeerConnection.IceGatheringState) {}
                override fun onIceCandidatesRemoved(c: Array<out IceCandidate>) {}
                override fun onAddStream(stream: MediaStream) {}
                override fun onRemoveStream(stream: MediaStream) {}
                override fun onDataChannel(dc: DataChannel) {}
                override fun onRenegotiationNeeded() {}
                override fun onAddTrack(r: RtpReceiver, streams: Array<out MediaStream>) {}
            }
        )

        val stream = peerConnectionFactory?.createLocalMediaStream("ARDAMS")
        localAudioTrack?.let { stream?.addTrack(it) }
        localVideoTrack?.let { stream?.addTrack(it) }
        stream?.let { peerConnection?.addStream(it) }
    }

    private fun observeSocketEvents() {
        socketManager.events.onEach { event ->
            when (event) {
                is SocketEvent.CallAccepted -> {
                    peerConnection?.setRemoteDescription(
                        object : SdpObserver {
                            override fun onSetSuccess() {}
                            override fun onCreateSuccess(sdp: SessionDescription) {}
                            override fun onCreateFailure(e: String) {}
                            override fun onSetFailure(e: String) {}
                        },
                        SessionDescription(SessionDescription.Type.ANSWER, event.sdp)
                    )
                }
                is SocketEvent.CallEnded -> endCall()
                is SocketEvent.IceCandidate -> {
                    // parse and add ICE candidate
                }
                else -> {}
            }
        }.launchIn(serviceScope)
    }

    private fun buildCallNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_CALLS)
            .setSmallIcon(R.drawable.ic_call)
            .setContentTitle("UzGram ${if (isVideoCall) "Video" else "Voice"} Call")
            .setContentText("Call in progress...")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        endCall()
        peerConnectionFactory?.dispose()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START_CALL = "com.uzgram.messenger.START_CALL"
        const val ACTION_END_CALL = "com.uzgram.messenger.END_CALL"
        const val CALL_NOTIFICATION_ID = 9002
    }
}
