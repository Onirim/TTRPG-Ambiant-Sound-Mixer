package com.oniri.ttrpgmixer.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.oniri.ttrpgmixer.playback.SlotId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "mixer_settings")

class SettingsRepository(private val context: Context) {

    private fun keyPrefix(slot: SlotId) = slot.name.lowercase()

    private fun uriKey(slot: SlotId) = stringPreferencesKey("${keyPrefix(slot)}_uri")
    private fun displayNameKey(slot: SlotId) = stringPreferencesKey("${keyPrefix(slot)}_display_name")
    private fun volumeKey(slot: SlotId) = floatPreferencesKey("${keyPrefix(slot)}_volume")
    private fun loopKey(slot: SlotId) = booleanPreferencesKey("${keyPrefix(slot)}_loop")
    private val themeIdKey = stringPreferencesKey("selected_theme_id")

    fun settingsFlow(slot: SlotId): Flow<SlotSettings> =
        context.dataStore.data.map { prefs ->
            SlotSettings(
                uriString = prefs[uriKey(slot)],
                displayName = prefs[displayNameKey(slot)],
                volume = prefs[volumeKey(slot)] ?: 1f,
                loop = prefs[loopKey(slot)] ?: true
            )
        }

    suspend fun saveFile(slot: SlotId, uriString: String, displayName: String?) {
        context.dataStore.edit { prefs ->
            prefs[uriKey(slot)] = uriString
            if (displayName != null) {
                prefs[displayNameKey(slot)] = displayName
            } else {
                prefs.remove(displayNameKey(slot))
            }
        }
    }

    suspend fun clearFile(slot: SlotId) {
        context.dataStore.edit { prefs ->
            prefs.remove(uriKey(slot))
            prefs.remove(displayNameKey(slot))
        }
    }

    suspend fun saveVolume(slot: SlotId, volume: Float) {
        context.dataStore.edit { prefs -> prefs[volumeKey(slot)] = volume }
    }

    suspend fun saveLoop(slot: SlotId, loop: Boolean) {
        context.dataStore.edit { prefs -> prefs[loopKey(slot)] = loop }
    }

    val themeIdFlow: Flow<String> = context.dataStore.data.map { prefs -> prefs[themeIdKey] ?: DEFAULT_THEME_ID }

    suspend fun saveThemeId(id: String) {
        context.dataStore.edit { prefs -> prefs[themeIdKey] = id }
    }

    companion object {
        const val DEFAULT_THEME_ID = "dragon_dream"
    }
}
