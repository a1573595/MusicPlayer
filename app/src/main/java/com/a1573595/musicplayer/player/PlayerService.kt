package com.a1573595.musicplayer.player

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class PlayerService : Service() {
    private lateinit var player: PlayerManager

    inner class LocalBinder : Binder() {
        // Return this instance of PlayerService so clients can call public methods
        val service: PlayerService = this@PlayerService
    }

    override fun onCreate() {
        super.onCreate()

        player = PlayerManager()
    }

    override fun onBind(intent: Intent): IBinder {
        return LocalBinder()
    }

    fun getPlayerManager() = player
}
