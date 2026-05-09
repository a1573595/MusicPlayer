package com.a1573595.musicplayer.domain.player

import com.a1573595.musicplayer.domain.song.Song
import java.io.Closeable
import java.io.FileDescriptor

interface PlaybackAudioSource {
    fun open(song: Song): PlaybackAudio?
}

class PlaybackAudio(
    val fileDescriptor: FileDescriptor,
    private val closeAction: () -> Unit = {}
) : Closeable {
    override fun close() {
        closeAction()
    }
}
