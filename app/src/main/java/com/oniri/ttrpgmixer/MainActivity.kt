package com.oniri.ttrpgmixer

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.oniri.ttrpgmixer.playback.SlotId
import com.oniri.ttrpgmixer.ui.MainScreen
import com.oniri.ttrpgmixer.ui.MixerViewModel
import com.oniri.ttrpgmixer.ui.theme.TtrpgMixerTheme

private val AUDIO_MIME_TYPES = arrayOf("audio/mpeg", "audio/mp3", "audio/*")

class MainActivity : ComponentActivity() {

    private val viewModel: MixerViewModel by viewModels()

    private lateinit var ambianceFilePicker: ActivityResultLauncher<Array<String>>
    private lateinit var musicFilePicker: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ambianceFilePicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { viewModel.onFilePicked(SlotId.AMBIANCE, it) }
        }
        musicFilePicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { viewModel.onFilePicked(SlotId.MUSIC, it) }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
                .launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            TtrpgMixerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ) {
                    val uiState by viewModel.uiState.collectAsState()
                    MainScreen(
                        uiState = uiState,
                        onLoadFile = { slot ->
                            when (slot) {
                                SlotId.AMBIANCE -> ambianceFilePicker.launch(AUDIO_MIME_TYPES)
                                SlotId.MUSIC -> musicFilePicker.launch(AUDIO_MIME_TYPES)
                            }
                        },
                        onPlayPause = viewModel::onPlayPause,
                        onVolumeChange = viewModel::onVolumeChange,
                        onLoopToggle = viewModel::onLoopToggle,
                        onSeek = viewModel::onSeek
                    )
                }
            }
        }
    }
}
