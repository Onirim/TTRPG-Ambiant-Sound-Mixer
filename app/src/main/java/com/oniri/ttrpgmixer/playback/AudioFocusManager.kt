package com.oniri.ttrpgmixer.playback

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager

enum class AudioFocusEvent { GAIN, LOSS, LOSS_TRANSIENT, LOSS_TRANSIENT_CAN_DUCK }

/**
 * A single shared focus request for the whole app: both slots' ExoPlayer instances have
 * handleAudioFocus = false, so they never fight each other for focus individually.
 */
class AudioFocusManager(
    context: Context,
    private val onFocusChanged: (AudioFocusEvent) -> Unit
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var focusRequest: AudioFocusRequest? = null

    private val focusListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> onFocusChanged(AudioFocusEvent.GAIN)
            AudioManager.AUDIOFOCUS_LOSS -> onFocusChanged(AudioFocusEvent.LOSS)
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> onFocusChanged(AudioFocusEvent.LOSS_TRANSIENT)
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> onFocusChanged(AudioFocusEvent.LOSS_TRANSIENT_CAN_DUCK)
        }
    }

    fun requestFocus(): Boolean {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(attributes)
            .setOnAudioFocusChangeListener(focusListener)
            .build()
        focusRequest = request
        return audioManager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    fun abandonFocus() {
        focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        focusRequest = null
    }
}
