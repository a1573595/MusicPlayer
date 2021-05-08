package com.a1573595.musicplayer.player

import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message

class AudioObserver(private val handler: Handler) : ContentObserver(handler) {
    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)

        if (selfChange) return

        uri?.lastPathSegment?.let {
            val b = Bundle()
            b.putString("songID", it)

            val msg = Message()
            msg.data = b

            handler.sendMessage(msg)
        }
    }
}