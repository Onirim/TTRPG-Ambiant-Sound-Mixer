package com.oniri.ttrpgmixer.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.oniri.ttrpgmixer.MainActivity
import com.oniri.ttrpgmixer.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlaybackService : Service(), PlaybackController {

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    private lateinit var audioFocusManager: AudioFocusManager
    private var hasAudioFocus = false
    private var duckedForFocusLoss = false
    private val resumeAfterTransientLoss = mutableSetOf<SlotId>()
    private var isForeground = false

    private inner class PlayerSlotHolder {
        val state = MutableStateFlow(SlotPlaybackState())
        val player: ExoPlayer = ExoPlayer.Builder(this@PlaybackService).build()

        init {
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus = */ false
            )
            player.setHandleAudioBecomingNoisy(true)
            player.repeatMode = Player.REPEAT_MODE_ONE
            player.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    state.update { it.copy(isPlaying = isPlaying) }
                    onAnyPlaybackChanged()
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        state.update { it.copy(durationMs = player.duration.coerceAtLeast(0), isAvailable = true) }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    state.update { it.copy(isAvailable = false, isPlaying = false) }
                }
            })
        }
    }

    private lateinit var holders: Map<SlotId, PlayerSlotHolder>

    inner class LocalBinder : Binder() {
        fun getController(): PlaybackController = this@PlaybackService
    }

    override fun onCreate() {
        super.onCreate()
        holders = mapOf(
            SlotId.AMBIANCE to PlayerSlotHolder(),
            SlotId.MUSIC to PlayerSlotHolder()
        )
        audioFocusManager = AudioFocusManager(this) { event -> handleFocusEvent(event) }
        startPositionPolling()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            holders.values.forEach { it.player.playWhenReady = false }
            stopForegroundCompat()
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        holders.values.forEach { it.player.release() }
        audioFocusManager.abandonFocus()
        serviceScope.cancel()
        super.onDestroy()
    }

    // --- PlaybackController ---

    override fun loadMedia(slot: SlotId, uri: Uri) {
        val holder = holders.getValue(slot)
        holder.state.update { it.copy(isAvailable = true, positionMs = 0, durationMs = 0) }
        holder.player.setMediaItem(MediaItem.fromUri(uri))
        holder.player.prepare()
    }

    override fun play(slot: SlotId) {
        val holder = holders.getValue(slot)
        if (!hasAudioFocus) {
            hasAudioFocus = audioFocusManager.requestFocus()
            if (!hasAudioFocus) return
        }
        ensureForeground()
        holder.player.playWhenReady = true
    }

    override fun pause(slot: SlotId) {
        holders.getValue(slot).player.playWhenReady = false
    }

    override fun setVolume(slot: SlotId, volume: Float) {
        val holder = holders.getValue(slot)
        holder.state.update { it.copy(volume = volume) }
        if (!duckedForFocusLoss) {
            holder.player.volume = volume
        }
    }

    override fun setLoop(slot: SlotId, loop: Boolean) {
        val holder = holders.getValue(slot)
        holder.state.update { it.copy(loop = loop) }
        holder.player.repeatMode = if (loop) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
    }

    override fun seekTo(slot: SlotId, positionMs: Long) {
        val holder = holders.getValue(slot)
        holder.player.seekTo(positionMs)
        holder.state.update { it.copy(positionMs = positionMs) }
    }

    override fun stateFlow(slot: SlotId): StateFlow<SlotPlaybackState> = holders.getValue(slot).state.asStateFlow()

    // --- internal ---

    private fun onAnyPlaybackChanged() {
        val anyPlaying = holders.values.any { it.state.value.isPlaying }
        if (!anyPlaying) {
            if (hasAudioFocus) {
                audioFocusManager.abandonFocus()
                hasAudioFocus = false
            }
            stopForegroundCompat()
        } else {
            updateNotification()
        }
    }

    private fun handleFocusEvent(event: AudioFocusEvent) {
        when (event) {
            AudioFocusEvent.LOSS -> {
                holders.values.forEach { it.player.playWhenReady = false }
                hasAudioFocus = false
            }
            AudioFocusEvent.LOSS_TRANSIENT -> {
                resumeAfterTransientLoss.clear()
                holders.forEach { (slot, holder) ->
                    if (holder.state.value.isPlaying) {
                        resumeAfterTransientLoss.add(slot)
                        holder.player.playWhenReady = false
                    }
                }
            }
            AudioFocusEvent.LOSS_TRANSIENT_CAN_DUCK -> {
                duckedForFocusLoss = true
                holders.values.forEach { it.player.volume = it.state.value.volume * 0.2f }
            }
            AudioFocusEvent.GAIN -> {
                if (duckedForFocusLoss) {
                    duckedForFocusLoss = false
                    holders.values.forEach { it.player.volume = it.state.value.volume }
                }
                resumeAfterTransientLoss.forEach { slot -> holders.getValue(slot).player.playWhenReady = true }
                resumeAfterTransientLoss.clear()
            }
        }
    }

    private fun startPositionPolling() {
        serviceScope.launch {
            while (true) {
                holders.values.forEach { holder ->
                    val player = holder.player
                    holder.state.update {
                        it.copy(
                            positionMs = player.currentPosition.coerceAtLeast(0),
                            durationMs = player.duration.coerceAtLeast(0)
                        )
                    }
                }
                delay(500)
            }
        }
    }

    // --- notification / foreground ---

    private fun ensureForeground() {
        if (isForeground) return
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        isForeground = true
    }

    private fun stopForegroundCompat() {
        if (!isForeground) return
        stopForeground(STOP_FOREGROUND_REMOVE)
        isForeground = false
    }

    private fun updateNotification() {
        if (!isForeground) return
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this, 0,
            Intent(this, PlaybackService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setContentIntent(openAppIntent)
            .addAction(0, getString(R.string.notification_action_stop), stopIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_LOW)
        )
    }

    companion object {
        private const val CHANNEL_ID = "playback_channel"
        private const val NOTIFICATION_ID = 1
        const val ACTION_STOP = "com.oniri.ttrpgmixer.action.STOP"
    }
}
