package com.a1573595.musicplayer

import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.os.Build

fun e2ePermissions(): Array<String> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(READ_MEDIA_AUDIO, POST_NOTIFICATIONS)
    } else {
        arrayOf(READ_EXTERNAL_STORAGE)
    }
}
