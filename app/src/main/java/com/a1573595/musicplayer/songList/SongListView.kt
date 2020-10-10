package com.a1573595.musicplayer.songList

import com.a1573595.musicplayer.BaseView
import com.a1573595.musicplayer.model.Song

interface SongListView : BaseView {
    fun showLoading()

    fun stopLoading()

    fun updateSongState(song: Song, isPlaying: Boolean)

    fun onSongClick()
}