package com.oniri.ttrpgmixer.ui

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.IBinder
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oniri.ttrpgmixer.MixerApplication
import com.oniri.ttrpgmixer.playback.PlaybackController
import com.oniri.ttrpgmixer.playback.PlaybackService
import com.oniri.ttrpgmixer.playback.SlotId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MixerViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = (application as MixerApplication).settingsRepository
    private val contentResolver = application.contentResolver

    private var controller: PlaybackController? = null

    private val _uiState = MutableStateFlow(MixerUiState())
    val uiState: StateFlow<MixerUiState> = _uiState.asStateFlow()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            controller = (service as PlaybackService.LocalBinder).getController()
            onControllerReady()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            controller = null
        }
    }

    init {
        val intent = Intent(application, PlaybackService::class.java)
        application.startService(intent)
        application.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun onControllerReady() {
        SlotId.entries.forEach { slot ->
            restoreSlot(slot)
            observePlaybackState(slot)
        }
    }

    private fun restoreSlot(slot: SlotId) {
        viewModelScope.launch {
            val settings = settingsRepository.settingsFlow(slot).first()
            var displayName = settings.displayName
            val uriString = settings.uriString
            if (uriString != null) {
                val uri = Uri.parse(uriString)
                val stillGranted = contentResolver.persistedUriPermissions.any { it.uri == uri && it.isReadPermission }
                if (stillGranted) {
                    controller?.loadMedia(slot, uri)
                } else {
                    settingsRepository.clearFile(slot)
                    displayName = null
                }
            }
            controller?.setVolume(slot, settings.volume)
            controller?.setLoop(slot, settings.loop)
            updateSlotUi(slot) { it.copy(displayName = displayName, volume = settings.volume, loop = settings.loop) }
        }
    }

    private fun observePlaybackState(slot: SlotId) {
        viewModelScope.launch {
            controller?.stateFlow(slot)?.let { flow ->
                flow.collect { state ->
                    updateSlotUi(slot) {
                        it.copy(
                            isPlaying = state.isPlaying,
                            isAvailable = state.isAvailable,
                            positionMs = state.positionMs,
                            durationMs = state.durationMs,
                            volume = state.volume,
                            loop = state.loop
                        )
                    }
                }
            }
        }
    }

    fun onFilePicked(slot: SlotId, uri: Uri) {
        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        viewModelScope.launch {
            val previous = settingsRepository.settingsFlow(slot).first()
            val previousUri = previous.uriString
            if (previousUri != null && previousUri != uri.toString()) {
                try {
                    contentResolver.releasePersistableUriPermission(Uri.parse(previousUri), Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (_: SecurityException) {
                    // permission already gone, nothing to release
                }
            }
            val displayName = DocumentFile.fromSingleUri(getApplication(), uri)?.name
            settingsRepository.saveFile(slot, uri.toString(), displayName)
            updateSlotUi(slot) { it.copy(displayName = displayName, isAvailable = true, positionMs = 0, durationMs = 0) }
            controller?.loadMedia(slot, uri)
        }
    }

    fun onPlayPause(slot: SlotId) {
        val playing = currentSlotState(slot).isPlaying
        if (playing) controller?.pause(slot) else controller?.play(slot)
    }

    fun onVolumeChange(slot: SlotId, volume: Float) {
        controller?.setVolume(slot, volume)
        viewModelScope.launch { settingsRepository.saveVolume(slot, volume) }
    }

    fun onLoopToggle(slot: SlotId, loop: Boolean) {
        controller?.setLoop(slot, loop)
        viewModelScope.launch { settingsRepository.saveLoop(slot, loop) }
    }

    fun onSeek(slot: SlotId, positionMs: Long) {
        controller?.seekTo(slot, positionMs)
    }

    private fun currentSlotState(slot: SlotId): SlotUiState = when (slot) {
        SlotId.AMBIANCE -> _uiState.value.ambiance
        SlotId.MUSIC -> _uiState.value.music
    }

    private fun updateSlotUi(slot: SlotId, transform: (SlotUiState) -> SlotUiState) {
        _uiState.update { current ->
            when (slot) {
                SlotId.AMBIANCE -> current.copy(ambiance = transform(current.ambiance))
                SlotId.MUSIC -> current.copy(music = transform(current.music))
            }
        }
    }

    override fun onCleared() {
        getApplication<Application>().unbindService(serviceConnection)
        super.onCleared()
    }
}
