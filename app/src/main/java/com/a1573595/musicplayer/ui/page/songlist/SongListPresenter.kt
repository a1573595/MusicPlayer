package com.a1573595.musicplayer.ui.page.songlist

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
    @Volatile
    private var currentFilterKey: String = ""

    fun setPlaybackController(
        player: PlaybackController,
        initialFilterKey: String = ""
    ) {
        this.player = player
        currentFilterKey = initialFilterKey

        loadSongList()
    }

    fun fetchSongState() {
        player.getSong()?.let {
            view.updateSongState(it, player.isPlaying())
        }
    }

    fun filterSong(key: String) {
        currentFilterKey = key

        scope.launch {
            val result = filterSongs(player.getSongList(), key)

            withContext(mainDispatcher) {
                if (key == currentFilterKey) {
                    filteredSongs = result
                    view.renderSongs(result.songs)
                }
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
            val filterKey = currentFilterKey
            val result = filterSongs(player.getSongList(), filterKey)

            withContext(mainDispatcher) {
                view.stopLoading()

                if (filterKey == currentFilterKey) {
                    filteredSongs = result
                    view.renderSongs(result.songs)
                }
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
