package com.a1573595.musicplayer.songList

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.util.Patterns
import androidx.core.net.toUri
import com.a1573595.musicplayer.BasePresenter
import com.a1573595.musicplayer.R
import com.a1573595.musicplayer.domain.player.PlaybackController
import com.a1573595.musicplayer.domain.song.FilterSongsUseCase
import com.a1573595.musicplayer.domain.song.FilteredSongs
import kotlinx.coroutines.*

class SongListPresenter constructor(
    view: SongListView,
    private val filterSongs: FilterSongsUseCase = FilterSongsUseCase(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + Job()),
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
) : BasePresenter<SongListView>(view) {

    private lateinit var player: PlaybackController

    private var filteredSongs: FilteredSongs = FilteredSongs(emptyList(), emptyList())

    fun setPlayerManager(player: PlaybackController) {
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

    fun downloadSong(url: String) {
        if (Patterns.WEB_URL.matcher(url).matches() && isSupport(url)) {
            val uri = url.toUri()

            val downloadManager: DownloadManager =
                view.context().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            val request: DownloadManager.Request = DownloadManager.Request(uri)
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_MUSIC,
                uri.lastPathSegment
            )
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            downloadManager.enqueue(request)
        } else {
            view.showToast(view.context().getString(R.string.unsupported_format))
        }
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

    private fun isSupport(extension: String): Boolean {
        return extension.endsWith("mp3") || extension.endsWith("wav")
                || extension.endsWith("ogg") || extension.endsWith("flac")
    }

    fun clear() {
        scope.cancel()
    }
}
