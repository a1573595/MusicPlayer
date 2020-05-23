package com.a1573595.musicplayer.playSong

import com.a1573595.musicplayer.BaseView
import com.a1573595.musicplayer.model.Song

interface PlaySongView : BaseView {
    fun updateSongState(song: Song, isPlaying: Boolean, progress: Int)

    fun showRepeat(isRepeat: Boolean)

    fun showRandom(isRandom: Boolean)
}