package com.oniri.ttrpgmixer

import android.app.Application
import com.oniri.ttrpgmixer.data.SettingsRepository

class MixerApplication : Application() {
    lateinit var settingsRepository: SettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepository(this)
    }
}
