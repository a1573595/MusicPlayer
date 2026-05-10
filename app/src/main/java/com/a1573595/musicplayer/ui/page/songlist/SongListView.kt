package com.a1573595.musicplayer.ui.page.songlist

import com.a1573595.musicplayer.ui.base.BaseView
import com.a1573595.musicplayer.domain.song.Song

interface SongListView : BaseView {
    fun showLoading()

    fun stopLoading()

    fun renderSongs(songs: List<Song>)

    fun updateSongState(song: Song, isPlaying: Boolean)

    fun onSongClick()
}
