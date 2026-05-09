package com.a1573595.musicplayer.ui.songlist

import com.a1573595.musicplayer.ui.base.BasePresenter
import com.a1573595.musicplayer.domain.player.PlaybackController
import com.a1573595.musicplayer.domain.song.FilterSongsUseCase
import com.a1573595.musicplayer.domain.song.FilteredSongs
import kotlinx.coroutines.*

class SongListPresenter(
    view: SongListView,
    private val filterSongs: FilterSongsUseCase = FilterSongsUseCase(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + Job()),
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
) : BasePresenter<SongListView>(view) {

    private lateinit var player: PlaybackController

    private var filteredSongs: FilteredSongs = FilteredSongs(emptyList(), emptyList())

    fun setPlaybackController(player: PlaybackController) {
        this.player = player

        loadSongList()
    }

    fun fetchSongState() {
        player.getSong()?.let {
            view.updateSongState(it, player.isPlaying())
        }
    }

    fun filterSong(key: String) {
        scope.launch {
            val result = filterSongs(player.getSongList(), key)
            filteredSongs = result

            withContext(mainDispatcher) {
                view.renderSongs(result.songs)
            }
        }
    }

    fun onSongPlay() {
        if (!player.isPlaying()) {
            player.play()
        } else {
            player.pause()
        }
    }

    fun onSongClick(index: Int) {
        val position = filteredSongs.originalPositionAt(index) ?: return

        view.onSongClick()
        playSong(position)
    }

    private fun loadSongList() {
        scope.launch {
            withContext(mainDispatcher) {
                view.showLoading()
            }

            player.readSong()
            filteredSongs = filterSongs(player.getSongList(), "")

            withContext(mainDispatcher) {
                view.stopLoading()
                view.renderSongs(filteredSongs.songs)
                fetchSongState()
            }
        }
    }

    private fun playSong(position: Int) {
        player.play(position)
    }

    fun clear() {
        scope.cancel()
    }
}
