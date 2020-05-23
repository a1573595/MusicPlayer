package com.a1573595.musicplayer.player

import android.app.Service
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.provider.MediaStore
import com.a1573595.musicplayer.R
import com.a1573595.musicplayer.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileDescriptor
import java.util.*

class PlayerService : Service() {
    private val uriExternal: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    private val playerManager: PlayerManager = PlayerManager()

    private val songList: ArrayList<Song> = ArrayList()
    private var playerPosition: Int = 0    // song queue position
    private var isPlaying: Boolean = false // mediaPlayer.isPlaying may take some time update status
    var isRepeat: Boolean = false
    var isRandom: Boolean = false

    inner class LocalBinder : Binder() {
        // Return this instance of PlayerService so clients can call public methods
        val service: PlayerService = this@PlayerService
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent): IBinder {
        return LocalBinder()
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)

        stopSelf()
    }

    suspend fun readSong() = withContext(Dispatchers.IO) {
        if(songList.isNotEmpty()) return@withContext

        contentResolver.query(
            uriExternal, null, null, null,
            null
        )?.use {
            val indexID: Int = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val indexTitle: Int = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)

            while (it.moveToNext()) {
                val id = it.getString(indexID)
                val title = it.getString(indexTitle)
                val audioUri = Uri.withAppendedPath(uriExternal, id)
                val fd = contentResolver.openFileDescriptor(audioUri, "r")?.fileDescriptor!!
                addSong(fd, id, title)
            }
        }
    }

    private fun addSong(fd: FileDescriptor, id: String, title: String) {
        val metaRetriever = MediaMetadataRetriever()
        metaRetriever.setDataSource(fd)

        val duration =
            metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val artist =
            metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        val author =
            metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR)

        songList.add(
            Song(
                id,
                title,
                artist ?: author ?: getString(R.string.unknown),
                duration.toLong()
            )
        )

        metaRetriever.release()
    }

    fun addPlayerObserver(o: Observer) = playerManager.addObserver(o)

    fun deletePlayerObserver(o: Observer) = playerManager.deleteObserver(o)

    fun isPlaying(): Boolean = isPlaying

    fun getSongList() = songList.toList()

    fun getSong(): Song? {
        return if (songList.size > 0) {
            songList[playerPosition]
        } else {
            null
        }
    }

    fun getProgress(): Int = playerManager.getCurrentProgress()
}
