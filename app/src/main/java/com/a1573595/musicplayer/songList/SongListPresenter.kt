package com.a1573595.musicplayer.songList

import android.util.SparseArray
import com.a1573595.musicplayer.BasePresenter
import com.a1573595.musicplayer.model.Song
import com.a1573595.musicplayer.player.PlayerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SongListPresenter constructor(
    private val view: SongListView,
    private val scope: CoroutineScope
) :
    BasePresenter<SongListView>(view) {
    private lateinit var player: PlayerService

    private lateinit var adapter: SongListAdapter
    private val songList: SparseArray<Song> = SparseArray()

    fun setPlayerManager(player: PlayerService) {
        this.player = player

        loadSongList()
    }

    fun setAdapter(adapter: SongListAdapter) {
        this.adapter = adapter
    }

    private fun loadSongList() {
        scope.launch {
            view.showLoading()

            player.readSong()
            player.getSongList().forEachIndexed { index, song -> songList.put(index, song) }

            adapter.putSong(songList)
            view.stopLoading()

            fetchSongState()
        }
    }

    fun fetchSongState() {
        val song = player.getSong() ?: return
        view.updateSongState(song, player.isPlaying())
    }

    fun filterSong(key: String) {
        scope.launch {
            songList.clear()
            player.getSongList().forEachIndexed { index, song ->
                if (song.name.contains(key, true)) {
                    songList.put(index, song)
                }
            }

            adapter.putSong(songList)
        }
    }
}