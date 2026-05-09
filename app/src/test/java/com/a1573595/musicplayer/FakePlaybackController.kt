package com.a1573595.musicplayer

import com.a1573595.musicplayer.domain.player.PlaybackController
import com.a1573595.musicplayer.model.Song

class FakePlaybackController(
    private val songs: List<Song> = emptyList(),
    private val currentSong: Song? = songs.firstOrNull(),
    private var playing: Boolean = false,
    private val progress: Int = 0
) : PlaybackController {
    var readSongCalls = 0
        private set
    var playCalls = 0
        private set
    var pauseCalls = 0
        private set
    var playedPosition: Int? = null
        private set
    var seekProgress: Int? = null
        private set
    var skipToNextCalls = 0
        private set
    var skipToPreviousCalls = 0
        private set

    override var isRepeat: Boolean = false
    override var isRandom: Boolean = false

    override suspend fun readSong() {
        readSongCalls++
    }

    override fun getSongList(): List<Song> = songs

    override fun getSong(): Song? = currentSong

    override fun isPlaying(): Boolean = playing

    override fun getProgress(): Int = progress

    override fun play() {
        playCalls++
        playing = true
    }

    override fun play(position: Int) {
        playedPosition = position
        playing = true
    }

    override fun pause() {
        pauseCalls++
        playing = false
    }

    override fun seekTo(progress: Int) {
        seekProgress = progress
    }

    override fun skipToNext() {
        skipToNextCalls++
    }

    override fun skipToPrevious() {
        skipToPreviousCalls++
    }
}
