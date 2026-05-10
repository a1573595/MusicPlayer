package com.a1573595.musicplayer

import android.content.Context
import android.content.Intent
import com.a1573595.musicplayer.data.player.PlayerService

fun Context.stopPlayerServiceForE2E() {
    stopService(Intent(this, PlayerService::class.java))
}
