package com.a1573595.musicplayer.ui.playsong

import com.a1573595.musicplayer.ui.base.BaseView
import com.a1573595.musicplayer.domain.song.Song

interface PlaySongView : BaseView {
    fun updateSongState(song: Song, isPlaying: Boolean, progress: Int)

    fun showRepeat(isRepeat: Boolean)

    fun showRandom(isRandom: Boolean)
}