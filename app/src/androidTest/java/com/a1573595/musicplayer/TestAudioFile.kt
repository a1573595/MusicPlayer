package com.a1573595.musicplayer

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

class TestAudioFile private constructor(
    private val context: Context,
    val uri: Uri
) {
    fun delete() {
        context.contentResolver.delete(uri, null, null)
    }

    companion object {
        fun insert(
            context: Context,
            title: String = "E2E Test Song",
            artist: String = "E2E Test Artist"
        ): TestAudioFile {
            val displayName = "${title.replace(' ', '_')}_${System.currentTimeMillis()}.wav"
            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Audio.Media.TITLE, title)
                put(MediaStore.Audio.Media.ARTIST, artist)
                put(MediaStore.Audio.Media.MIME_TYPE, "audio/wav")
                put(MediaStore.Audio.Media.IS_MUSIC, 1)
                put(MediaStore.Audio.Media.DURATION, 1000L)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/MusicPlayerE2E")
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val uri = checkNotNull(
                resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
            ) {
                "Failed to insert E2E audio into MediaStore"
            }

            resolver.openOutputStream(uri)?.use { output ->
                output.write(silentWav())
            } ?: error("Failed to open output stream for E2E audio")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Audio.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }

            return TestAudioFile(context, uri)
        }

        private fun silentWav(): ByteArray {
            val sampleRate = 8000
            val bitsPerSample = 16
            val channels = 1
            val durationSeconds = 1
            val dataSize = sampleRate * channels * bitsPerSample / 8 * durationSeconds
            val bytes = ByteArray(44 + dataSize)

            bytes.writeAscii(0, "RIFF")
            bytes.writeIntLE(4, 36 + dataSize)
            bytes.writeAscii(8, "WAVE")
            bytes.writeAscii(12, "fmt ")
            bytes.writeIntLE(16, 16)
            bytes.writeShortLE(20, 1)
            bytes.writeShortLE(22, channels)
            bytes.writeIntLE(24, sampleRate)
            bytes.writeIntLE(28, sampleRate * channels * bitsPerSample / 8)
            bytes.writeShortLE(32, channels * bitsPerSample / 8)
            bytes.writeShortLE(34, bitsPerSample)
            bytes.writeAscii(36, "data")
            bytes.writeIntLE(40, dataSize)

            return bytes
        }

        private fun ByteArray.writeAscii(offset: Int, value: String) {
            value.forEachIndexed { index, char ->
                this[offset + index] = char.code.toByte()
            }
        }

        private fun ByteArray.writeIntLE(offset: Int, value: Int) {
            this[offset] = value.toByte()
            this[offset + 1] = (value shr 8).toByte()
            this[offset + 2] = (value shr 16).toByte()
            this[offset + 3] = (value shr 24).toByte()
        }

        private fun ByteArray.writeShortLE(offset: Int, value: Int) {
            this[offset] = value.toByte()
            this[offset + 1] = (value shr 8).toByte()
        }
    }
}
