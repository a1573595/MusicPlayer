package com.a1573595.musicplayer.player

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import com.a1573595.musicplayer.R
import com.a1573595.musicplayer.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileDescriptor
import java.util.*

class PlayerManager(private val context: Context) : Observable() {
    private val mediaPlayer: MediaPlayer = MediaPlayer()
    private val uriExternal: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    private val songQueue: ArrayList<Song> = ArrayList()

    suspend fun readSong() = withContext(Dispatchers.IO) {
        songQueue.clear()

        context.contentResolver.query(
            uriExternal, null, null, null,
            null
        )?.use {
            val indexID: Int = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val indexTitle: Int = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)

            while (it.moveToNext()) {
                val id = it.getString(indexID)
                val title = it.getString(indexTitle)
                val audioUri = Uri.withAppendedPath(uriExternal, id)
                val fd = context.contentResolver.openFileDescriptor(audioUri, "r")?.fileDescriptor!!
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

        songQueue.add(
            Song(
                id,
                title,
                artist ?: author ?: context.getString(R.string.unknown),
                duration.toLong()
            )
        )

        metaRetriever.release()
    }

    fun getSongList() = songQueue.toList()
}