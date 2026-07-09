package com.oniri.ttrpgmixer.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.oniri.ttrpgmixer.R

/**
 * Everything that varies between visual themes: background art, palette, and per-slot
 * corner motifs. Add a new theme by adding one more [AppTheme] instance to [AppThemes.all].
 */
data class AppTheme(
    val id: String,
    val displayName: String,
    @DrawableRes val backgroundRes: Int,
    val ambianceInk: Color,
    val musicInk: Color,
    val frameGold: Color,
    val gildedHighlight: Color,
    val panelTop: Color,
    val panelBottom: Color,
    val woodButton: Color,
    val stoneMuted: Color,
    val stoneDark: Color,
    val background: Color,
    val onBackground: Color,
    val musicMotif: String,
    val ambianceMotif: String
)

val DragonDreamTheme = AppTheme(
    id = "dragon_dream",
    displayName = "Rêve de Dragon",
    backgroundRes = R.drawable.bg_grimoire,
    ambianceInk = Color(0xFF7B9BC2),
    musicInk = Color(0xFFC9A24B),
    frameGold = Color(0xFF8A6D3B),
    gildedHighlight = Color(0xFFD9B968),
    panelTop = Color(0xFF2A2013),
    panelBottom = Color(0xFF120C08),
    woodButton = Color(0xFF3A2A18),
    stoneMuted = Color(0xFF6B5738),
    stoneDark = Color(0xFF2B2118),
    background = Color(0xFF14110C),
    onBackground = Color(0xFFEDE0C8),
    musicMotif = "♫",
    ambianceMotif = "❧"
)

object AppThemes {
    val all = listOf(DragonDreamTheme)
    val default = DragonDreamTheme
    fun byId(id: String): AppTheme = all.firstOrNull { it.id == id } ?: default
}
