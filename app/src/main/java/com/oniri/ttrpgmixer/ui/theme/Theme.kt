package com.oniri.ttrpgmixer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GrimoireColors = darkColorScheme(
    primary = MusicInk,
    secondary = AmbianceInk,
    background = DarkBackground,
    surface = PanelDarkTop,
    onBackground = OnDark,
    onSurface = OnDark
)

@Composable
fun TtrpgMixerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GrimoireColors,
        typography = AppTypography,
        content = content
    )
}
