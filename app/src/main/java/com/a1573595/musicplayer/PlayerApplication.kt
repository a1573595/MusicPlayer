package com.a1573595.musicplayer

import android.app.Application
import timber.log.Timber

class PlayerApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}