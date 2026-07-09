package com.oniri.ttrpgmixer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppTheme = staticCompositionLocalOf { AppThemes.default }

@Composable
fun TtrpgMixerTheme(
    appTheme: AppTheme = AppThemes.default,
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = appTheme.musicInk,
        secondary = appTheme.ambianceInk,
        background = appTheme.background,
        surface = appTheme.panelTop,
        onBackground = appTheme.onBackground,
        onSurface = appTheme.onBackground
    )
    CompositionLocalProvider(LocalAppTheme provides appTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
