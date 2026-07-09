package com.oniri.ttrpgmixer.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.oniri.ttrpgmixer.R

/**
 * Everything that varies between visual themes: background art, palette, frame/button
 * silhouette, and per-slot corner motifs. Add a new theme by adding one more [AppTheme]
 * instance to [AppThemes.all].
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
    val ambianceMotif: String,
    val cardShape: Shape,
    val buttonShape: Shape
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
    ambianceMotif = "❧",
    cardShape = RoundedCornerShape(18.dp),
    buttonShape = RoundedCornerShape(8.dp)
)

val MysteryModernTheme = AppTheme(
    id = "mystery_modern",
    displayName = "Mystère moderne",
    backgroundRes = R.drawable.bg_mystery,
    ambianceInk = Color(0xFF5C8FA8),
    musicInk = Color(0xFFE0A040),
    frameGold = Color(0xFF7A8C99),
    gildedHighlight = Color(0xFFF0B25B),
    panelTop = Color(0xFF15222C),
    panelBottom = Color(0xFF06090D),
    woodButton = Color(0xFF1B2731),
    stoneMuted = Color(0xFF4A5A66),
    stoneDark = Color(0xFF141B21),
    background = Color(0xFF0A0F13),
    onBackground = Color(0xFFE4E9ED),
    musicMotif = "♠",
    ambianceMotif = "☂",
    // Only the top corners are cut, evoking an Art Deco archway/portal silhouette.
    cardShape = CutCornerShape(topStart = 20.dp, topEnd = 20.dp),
    buttonShape = CutCornerShape(topStart = 10.dp, topEnd = 10.dp)
)

object AppThemes {
    val all = listOf(DragonDreamTheme, MysteryModernTheme)
    val default = DragonDreamTheme
    fun byId(id: String): AppTheme = all.firstOrNull { it.id == id } ?: default
}
