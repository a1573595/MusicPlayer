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
import com.a1573595.musicplayer.player.PlayerManager.Companion.ACTION_COMPLETE
import com.a1573595.musicplayer.player.PlayerManager.Companion.ACTION_PAUSE
import com.a1573595.musicplayer.player.PlayerManager.Companion.ACTION_PLAY
import com.a1573595.musicplayer.player.PlayerManager.Companion.ACTION_STOP
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileDescriptor
import java.io.FileNotFoundException
import java.util.*

class PlayerService : Service(), Observer {
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

        addPlayerObserver(this)
    }

    override fun onBind(intent: Intent): IBinder {
        return LocalBinder()
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)

        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()

        deletePlayerObserver(this)
        playerManager.stop()
    }

    override fun update(o: Observable?, any: Any?) {
        when (any) {
            ACTION_COMPLETE -> {
                playerManager.setPlayerProgress(0)

                when {
                    isRepeat -> play()
                    isRandom -> play((0 until songList.size).random())
                    else -> skipToNext()
                }
            }
            ACTION_PLAY, ACTION_PAUSE -> {

            }
            ACTION_STOP -> {
                isPlaying = false
                stopForeground(true)
            }
        }
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

    fun getProgress(): Int = playerManager.getPlayerProgress()

    fun play(position: Int = playerPosition) {
        isPlaying = true

        if (position != playerPosition) {   // Is different song
            playerManager.setPlayerProgress(0)
        }

        playerPosition = when {
            songList.size < 1 -> {
                return
            }
            position >= songList.size -> 0
            position < 0 -> songList.lastIndex
            else -> position
        }

        val audioUri = Uri.withAppendedPath(uriExternal, songList[playerPosition].id)

        try {
            val fd = contentResolver.openFileDescriptor(audioUri, "r")?.fileDescriptor!!
            playerManager.play(fd)
        } catch (exception: FileNotFoundException) {
            play(playerPosition + 1)
        }
    }

    fun pause() {
        isPlaying = false

        playerManager.pause()
    }

    fun seekTo(progress: Int) {
        if (isPlaying) {
            playerManager.seekTo(progress)
        } else {
            playerManager.setPlayerProgress(progress)
            play()
        }
    }

    fun skipToNext() {
        if (!isRandom) {
            play(playerPosition + 1)
        } else {
            play((0 until songList.size).random())
        }
    }

    fun skipToPrevious() {
        if (!isRandom) {
            play(playerPosition - 1)
        } else {
            play((0 until songList.size).random())
        }
    }
}
