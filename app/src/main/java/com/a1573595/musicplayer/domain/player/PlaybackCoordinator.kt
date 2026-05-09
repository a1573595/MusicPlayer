package com.a1573595.musicplayer.domain.player

import com.a1573595.musicplayer.domain.player.PlaybackEngine.Companion.ACTION_COMPLETE
import com.a1573595.musicplayer.domain.player.PlaybackEngine.Companion.ACTION_PAUSE
import com.a1573595.musicplayer.domain.player.PlaybackEngine.Companion.ACTION_PLAY
import com.a1573595.musicplayer.domain.player.PlaybackEngine.Companion.ACTION_STOP
import com.a1573595.musicplayer.domain.song.Song

class PlaybackCoordinator(
    private val queue: PlaybackQueue,
    private val engine: PlaybackEngine,
    private val audioSource: PlaybackAudioSource,
    private val onPlaybackStart: () -> Unit,
    private val onPlaybackStateChanged: () -> Unit,
    private val onPlaybackUnavailable: () -> Unit
) {
    var isPlaying: Boolean = false
        private set

    var isRepeat: Boolean
        get() = queue.isRepeat
        set(value) {
            queue.isRepeat = value
        }

    var isRandom: Boolean
        get() = queue.isRandom
        set(value) {
            queue.isRandom = value
        }

    fun songs(): List<Song> = queue.songs()

    fun currentSong(): Song? = queue.currentSong()

    fun replaceSongs(songs: List<Song>) {
        queue.replaceSongs(songs)
    }

    fun addIfAbsent(song: Song): Boolean = queue.addIfAbsent(song)

    fun progress(): Int = engine.progress

    fun play(position: Int = queue.currentIndex) {
        val previousIndex = queue.currentIndex
        val song = queue.play(position)
        if (song == null) {
            notifyPlaybackUnavailable()
            return
        }

        if (position != previousIndex) {
            engine.progress = 0
        }

        playCurrentSong(song)
    }

    fun pause() {
        isPlaying = false
        engine.pause()
    }

    fun seekTo(progress: Int) {
        if (isPlaying) {
            engine.seekTo(progress)
        } else {
            engine.progress = progress
            play()
        }
    }

    fun skipToNext() {
        val previousIndex = queue.currentIndex
        val song = queue.next()
        if (song == null) {
            notifyPlaybackUnavailable()
            return
        }

        if (queue.currentIndex != previousIndex) {
            engine.progress = 0
        }

        playCurrentSong(song)
    }

    fun skipToPrevious() {
        val previousIndex = queue.currentIndex
        val song = queue.previous()
        if (song == null) {
            notifyPlaybackUnavailable()
            return
        }

        if (queue.currentIndex != previousIndex) {
            engine.progress = 0
        }

        playCurrentSong(song)
    }

    fun onEngineEvent(event: String) {
        when (event) {
            ACTION_COMPLETE -> {
                engine.progress = 0
                isPlaying = false

                if (isRepeat) {
                    play()
                } else {
                    skipToNext()
                }
            }

            ACTION_PLAY -> {
                isPlaying = true
                onPlaybackStateChanged()
            }

            ACTION_PAUSE -> {
                isPlaying = false
                onPlaybackStateChanged()
            }

            ACTION_STOP -> {
                isPlaying = false
            }
        }
    }

    private fun playCurrentSong(song: Song) {
        val audio = audioSource.open(song)
        if (audio == null) {
            queue.removeCurrent()
            play()
            return
        }

        audio.use {
            onPlaybackStart()

            if (!engine.play(it.fileDescriptor)) {
                notifyPlaybackUnavailable()
            }
        }
    }

    private fun notifyPlaybackUnavailable() {
        isPlaying = false
        engine.progress = 0
        onPlaybackUnavailable()
    }
}
