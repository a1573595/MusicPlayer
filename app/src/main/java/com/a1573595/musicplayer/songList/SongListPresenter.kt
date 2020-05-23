package com.a1573595.musicplayer.songList

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Patterns
import android.util.SparseArray
import com.a1573595.musicplayer.BasePresenter
import com.a1573595.musicplayer.R
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

    fun fetchSongState() {
        val song = player.getSong() ?: return
        view.updateSongState(song, player.isPlaying())
    }

    fun filterSong(key: String) {
        if (!::player.isInitialized) return

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

    fun playSong(position: Int) {
        player.play(position)
    }

    fun onSongPlay() {
        if (!player.isPlaying()) {
            player.play()
        } else {
            player.pause()
        }
    }

    fun downloadSong(url: String) {
        if (Patterns.WEB_URL.matcher(url).matches() && isSupport(url)) {
            val uri: Uri = Uri.parse(url)

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
            view.showLoading()

            player.readSong()
            player.getSongList().forEachIndexed { index, song -> songList.put(index, song) }

            adapter.putSong(songList)
            view.stopLoading()

            fetchSongState()
        }
    }

    private fun isSupport(extension: String): Boolean {
        return extension.endsWith("mp3") || extension.endsWith("wav")
                || extension.endsWith("ogg") || extension.endsWith("flac")
    }
}