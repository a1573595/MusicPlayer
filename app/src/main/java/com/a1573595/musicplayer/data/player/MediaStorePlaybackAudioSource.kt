package com.a1573595.musicplayer.data.player

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import com.a1573595.musicplayer.domain.player.PlaybackAudio
import com.a1573595.musicplayer.domain.player.PlaybackAudioSource
import com.a1573595.musicplayer.domain.song.Song
import timber.log.Timber

class MediaStorePlaybackAudioSource(
    private val contentResolver: ContentResolver,
    private val audioUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
) : PlaybackAudioSource {
    override fun open(song: Song): PlaybackAudio? {
        val songUri = Uri.withAppendedPath(audioUri, song.id)
        val descriptor = try {
            contentResolver.openFileDescriptor(songUri, "r")
        } catch (e: Exception) {
            Timber.e(e)
            null
        }

        return descriptor?.let { parcelFileDescriptor ->
            PlaybackAudio(parcelFileDescriptor.fileDescriptor) {
                parcelFileDescriptor.close()
            }
        }
    }
}
