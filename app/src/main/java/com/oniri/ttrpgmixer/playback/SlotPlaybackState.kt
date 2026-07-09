package com.oniri.ttrpgmixer.playback

data class SlotPlaybackState(
    val isPlaying: Boolean = false,
    val isAvailable: Boolean = true,
    val volume: Float = 1f,
    val loop: Boolean = true,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L
)
