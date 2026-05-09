package com.a1573595.musicplayer.data.song

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import com.a1573595.musicplayer.R
import com.a1573595.musicplayer.domain.song.SongRepository
import com.a1573595.musicplayer.model.Song
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.FileDescriptor

class MediaStoreSongRepository(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : SongRepository {
    private val contentResolver = context.contentResolver
    private val audioUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    override suspend fun loadSongs(): List<Song> = withContext(ioDispatcher) {
        val songs = mutableListOf<Song>()

        contentResolver.query(audioUri, null, null, null, null)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)

            while (cursor.moveToNext()) {
                val id = cursor.getString(idColumn)
                val title = cursor.getString(titleColumn)
                findSongById(id, title)?.let { song ->
                    if (!songs.contains(song)) {
                        songs += song
                    }
                }
            }
        }

        songs
    }

    override suspend fun findSong(id: String): Song? = withContext(ioDispatcher) {
        val songUri = Uri.withAppendedPath(audioUri, id)
        findSongById(id, getSongTitle(songUri))
    }

    private fun findSongById(id: String, title: String): Song? {
        val songUri = Uri.withAppendedPath(audioUri, id)

        return try {
            contentResolver.openFileDescriptor(songUri, "r")?.use {
                createSong(it.fileDescriptor, id, title)
            }
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    private fun createSong(fd: FileDescriptor, id: String, title: String): Song? {
        if (!fd.valid()) {
            return null
        }

        val metadataRetriever = MediaMetadataRetriever()
        return try {
            metadataRetriever.setDataSource(fd)

            val duration =
                metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val artist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val author = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR)

            if (duration.isNullOrEmpty()) {
                return null
            }

            Song(
                id = id,
                name = title,
                author = artist ?: author ?: context.getString(R.string.unknown),
                duration = duration.toLong()
            )
        } catch (e: Exception) {
            Timber.e(e)
            null
        } finally {
            metadataRetriever.release()
        }
    }

    private fun getSongTitle(uri: Uri): String {
        var title: String? = uri.lastPathSegment

        try {
            contentResolver.query(uri, null, null, null, null)?.use {
                if (it.moveToNext()) {
                    title = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }

        return title ?: ""
    }
}
