package com.oniri.ttrpgmixer.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.oniri.ttrpgmixer.R
import com.oniri.ttrpgmixer.playback.SlotId

@Composable
fun MainScreen(
    uiState: MixerUiState,
    onLoadFile: (SlotId) -> Unit,
    onPlayPause: (SlotId) -> Unit,
    onVolumeChange: (SlotId, Float) -> Unit,
    onLoopToggle: (SlotId, Boolean) -> Unit,
    onSeek: (SlotId, Long) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.bg_grimoire),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF120C08).copy(alpha = 0.45f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SlotPanel(
                title = stringResource(R.string.slot_music),
                state = uiState.music,
                accentColor = MaterialTheme.colorScheme.primary,
                onLoadFile = { onLoadFile(SlotId.MUSIC) },
                onPlayPause = { onPlayPause(SlotId.MUSIC) },
                onVolumeChange = { onVolumeChange(SlotId.MUSIC, it) },
                onLoopToggle = { onLoopToggle(SlotId.MUSIC, it) },
                onSeek = { onSeek(SlotId.MUSIC, it) },
                modifier = Modifier.weight(1f)
            )
            SlotPanel(
                title = stringResource(R.string.slot_ambiance),
                state = uiState.ambiance,
                accentColor = MaterialTheme.colorScheme.secondary,
                onLoadFile = { onLoadFile(SlotId.AMBIANCE) },
                onPlayPause = { onPlayPause(SlotId.AMBIANCE) },
                onVolumeChange = { onVolumeChange(SlotId.AMBIANCE, it) },
                onLoopToggle = { onLoopToggle(SlotId.AMBIANCE, it) },
                onSeek = { onSeek(SlotId.AMBIANCE, it) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
