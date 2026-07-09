package com.oniri.ttrpgmixer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.oniri.ttrpgmixer.R
import com.oniri.ttrpgmixer.ui.theme.FrameGold
import com.oniri.ttrpgmixer.ui.theme.GildedHighlight
import com.oniri.ttrpgmixer.ui.theme.PanelDarkBottom
import com.oniri.ttrpgmixer.ui.theme.PanelDarkTop
import com.oniri.ttrpgmixer.ui.theme.StoneDark
import com.oniri.ttrpgmixer.ui.theme.StoneMuted
import com.oniri.ttrpgmixer.ui.theme.WoodButton

private val cardShape = RoundedCornerShape(18.dp)
private val buttonShape = RoundedCornerShape(8.dp)

@Composable
fun SlotPanel(
    title: String,
    state: SlotUiState,
    accentColor: Color,
    cornerMotif: String,
    onLoadFile: () -> Unit,
    onPlayPause: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onLoopToggle: (Boolean) -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        // Frame: translucent parchment/leather wash, light enough to let the artwork show through.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(cardShape)
                .background(
                    Brush.verticalGradient(listOf(PanelDarkTop.copy(alpha = 0.38f), PanelDarkBottom.copy(alpha = 0.52f))),
                    cardShape
                )
                .border(2.dp, FrameGold.copy(alpha = 0.85f), cardShape)
        )
        // Inner hairline, evokes a Renaissance picture-frame molding.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp)
                .border(1.dp, GildedHighlight.copy(alpha = 0.45f), cardShape)
        )

        val motifStyle = MaterialTheme.typography.titleMedium.copy(color = accentColor.copy(alpha = 0.85f))
        Text(cornerMotif, style = motifStyle, modifier = Modifier.align(Alignment.TopStart).padding(12.dp))
        Text(cornerMotif, style = motifStyle, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp))
        Text(cornerMotif, style = motifStyle, modifier = Modifier.align(Alignment.BottomStart).padding(12.dp))
        Text(cornerMotif, style = motifStyle, modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp))

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = accentColor
                )
                Spacer(
                    Modifier
                        .padding(top = 4.dp)
                        .height(1.dp)
                        .width(72.dp)
                        .background(FrameGold.copy(alpha = 0.8f))
                )
                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onPlayPause, shape = buttonShape, colors = woodButtonColors(), border = woodButtonBorder()) {
                        Text(if (state.isPlaying) "⏸" else "▶")
                    }
                    Button(onClick = onLoadFile, shape = buttonShape, colors = woodButtonColors(), border = woodButtonBorder()) {
                        Text(stringResource(R.string.action_load_file))
                    }
                }

                Spacer(Modifier.height(6.dp))
                Text(
                    text = state.displayName?.let { name ->
                        if (state.isAvailable) name else stringResource(R.string.file_unavailable)
                    } ?: stringResource(R.string.no_file_loaded),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(10.dp))

                var isSeeking by remember { mutableStateOf(false) }
                var seekPositionMs by remember { mutableFloatStateOf(0f) }
                val durationMs = state.durationMs.coerceAtLeast(1L).toFloat()
                val displayedPositionMs = if (isSeeking) seekPositionMs else state.positionMs.toFloat().coerceIn(0f, durationMs)

                Slider(
                    value = displayedPositionMs,
                    valueRange = 0f..durationMs,
                    enabled = state.durationMs > 0,
                    onValueChange = {
                        isSeeking = true
                        seekPositionMs = it
                    },
                    onValueChangeFinished = {
                        onSeek(seekPositionMs.toLong())
                        isSeeking = false
                    },
                    colors = gildedSliderColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatDurationMs(displayedPositionMs.toLong()), style = MaterialTheme.typography.labelSmall)
                    Text(formatDurationMs(state.durationMs), style = MaterialTheme.typography.labelSmall)
                }

                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.label_loop))
                    Spacer(Modifier.width(8.dp))
                    Switch(checked = state.loop, onCheckedChange = onLoopToggle, colors = gildedSwitchColors())
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(64.dp)
                    .padding(start = 8.dp)
            ) {
                Text(stringResource(R.string.label_volume), style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(8.dp))
                VerticalSlider(
                    value = state.volume,
                    onValueChange = onVolumeChange,
                    modifier = Modifier
                        .height(160.dp)
                        .width(48.dp)
                )
            }
        }
    }
}

@Composable
private fun woodButtonColors() = ButtonDefaults.buttonColors(
    containerColor = WoodButton,
    contentColor = GildedHighlight
)

private fun woodButtonBorder() = BorderStroke(1.dp, FrameGold.copy(alpha = 0.8f))

@Composable
private fun gildedSwitchColors() = SwitchDefaults.colors(
    checkedThumbColor = GildedHighlight,
    checkedTrackColor = WoodButton,
    checkedBorderColor = FrameGold,
    uncheckedThumbColor = StoneMuted,
    uncheckedTrackColor = StoneDark,
    uncheckedBorderColor = FrameGold.copy(alpha = 0.6f)
)

@Composable
private fun gildedSliderColors() = SliderDefaults.colors(
    thumbColor = GildedHighlight,
    activeTrackColor = FrameGold,
    inactiveTrackColor = StoneDark,
    disabledThumbColor = StoneMuted,
    disabledActiveTrackColor = StoneDark,
    disabledInactiveTrackColor = StoneDark
)

@Composable
private fun VerticalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        colors = gildedSliderColors(),
        modifier = modifier
            .graphicsLayer { rotationZ = 270f }
            .layout { measurable, constraints ->
                val placeable = measurable.measure(
                    Constraints(
                        minWidth = constraints.minHeight,
                        maxWidth = constraints.maxHeight,
                        minHeight = constraints.minWidth,
                        maxHeight = constraints.maxWidth
                    )
                )
                layout(placeable.height, placeable.width) {
                    placeable.place(
                        x = -(placeable.width / 2 - placeable.height / 2),
                        y = -(placeable.height / 2 - placeable.width / 2)
                    )
                }
            }
    )
}

private fun formatDurationMs(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
