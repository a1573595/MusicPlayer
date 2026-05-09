package com.a1573595.musicplayer.domain.player

import com.a1573595.musicplayer.model.Song

interface PlaybackController {
    var isRepeat: Boolean
    var isRandom: Boolean

    suspend fun readSong()

    fun getSongList(): List<Song>

    fun getSong(): Song?

    fun isPlaying(): Boolean

    fun getProgress(): Int

    fun play()

    fun play(position: Int)

    fun pause()

    fun seekTo(progress: Int)

    fun skipToNext()

    fun skipToPrevious()
}
