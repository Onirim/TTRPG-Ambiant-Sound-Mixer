package com.oniri.ttrpgmixer.ui

import com.oniri.ttrpgmixer.ui.theme.AppThemes

data class SlotUiState(
    val displayName: String? = null,
    val volume: Float = 1f,
    val loop: Boolean = true,
    val isPlaying: Boolean = false,
    val isAvailable: Boolean = true,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L
)

data class MixerUiState(
    val ambiance: SlotUiState = SlotUiState(),
    val music: SlotUiState = SlotUiState(),
    val themeId: String = AppThemes.default.id
)
