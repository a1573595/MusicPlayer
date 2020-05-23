package com.a1573595.musicplayer.songList

import android.util.SparseArray
import com.a1573595.musicplayer.BasePresenter
import com.a1573595.musicplayer.model.Song
import com.a1573595.musicplayer.player.PlayerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class SongListPresenter constructor(private val view: SongListView) :
    BasePresenter<SongListView>(view) {
    private lateinit var player: PlayerManager

    private lateinit var adapter: SongListAdapter
    private val filteredList: SparseArray<Song> = SparseArray()

    fun setPlayerManager(player: PlayerManager) {
        this.player = player

        loadSongList()
    }

    fun setAdapter(adapter: SongListAdapter) {
        this.adapter = adapter
    }

    private fun loadSongList() {
        MainScope().launch(Dispatchers.IO) {
            view.showLoading()

            if (player.getSongList().isEmpty()) {
                player.readSong()
            }

            player.getSongList().forEachIndexed { index, song -> filteredList.put(index, song) }
            adapter.putSong(filteredList)
            view.stopLoading()
        }

        fetchSongState()
    }

    fun fetchSongState() {
//        val song = player.getSong()
//        view.updateSongState()
    }
}