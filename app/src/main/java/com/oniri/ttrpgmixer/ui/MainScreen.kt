package com.oniri.ttrpgmixer.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.oniri.ttrpgmixer.R
import com.oniri.ttrpgmixer.playback.SlotId
import com.oniri.ttrpgmixer.ui.theme.AppThemes
import com.oniri.ttrpgmixer.ui.theme.LocalAppTheme

@Composable
fun MainScreen(
    uiState: MixerUiState,
    onLoadFile: (SlotId) -> Unit,
    onPlayPause: (SlotId) -> Unit,
    onVolumeChange: (SlotId, Float) -> Unit,
    onLoopToggle: (SlotId, Boolean) -> Unit,
    onSeek: (SlotId, Long) -> Unit,
    onThemeSelected: (String) -> Unit
) {
    val theme = LocalAppTheme.current
    var showThemePicker by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(theme.backgroundRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF120C08).copy(alpha = 0.18f))
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
                cornerMotif = theme.musicMotif,
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
                cornerMotif = theme.ambianceMotif,
                onLoadFile = { onLoadFile(SlotId.AMBIANCE) },
                onPlayPause = { onPlayPause(SlotId.AMBIANCE) },
                onVolumeChange = { onVolumeChange(SlotId.AMBIANCE, it) },
                onLoopToggle = { onLoopToggle(SlotId.AMBIANCE, it) },
                onSeek = { onSeek(SlotId.AMBIANCE, it) },
                modifier = Modifier.weight(1f)
            )
        }

        IconButton(
            onClick = { showThemePicker = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .safeDrawingPadding()
                .padding(8.dp)
        ) {
            Text(
                text = "⚙",
                style = MaterialTheme.typography.titleLarge,
                color = theme.gildedHighlight
            )
        }
    }

    if (showThemePicker) {
        ThemePickerDialog(
            currentThemeId = theme.id,
            onThemeSelected = {
                onThemeSelected(it)
                showThemePicker = false
            },
            onDismiss = { showThemePicker = false }
        )
    }
}

@Composable
private fun ThemePickerDialog(
    currentThemeId: String,
    onThemeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        },
        title = { Text(stringResource(R.string.theme_picker_title)) },
        text = {
            Column {
                AppThemes.all.forEach { candidate ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(candidate.id) }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = candidate.id == currentThemeId,
                            onClick = { onThemeSelected(candidate.id) }
                        )
                        Text(candidate.displayName, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    )
}
