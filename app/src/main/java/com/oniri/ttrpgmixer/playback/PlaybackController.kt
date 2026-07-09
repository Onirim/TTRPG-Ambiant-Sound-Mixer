package com.oniri.ttrpgmixer.playback

import android.net.Uri
import kotlinx.coroutines.flow.StateFlow

interface PlaybackController {
    fun loadMedia(slot: SlotId, uri: Uri)
    fun play(slot: SlotId)
    fun pause(slot: SlotId)
    fun setVolume(slot: SlotId, volume: Float)
    fun setLoop(slot: SlotId, loop: Boolean)
    fun seekTo(slot: SlotId, positionMs: Long)
    fun stateFlow(slot: SlotId): StateFlow<SlotPlaybackState>
}
