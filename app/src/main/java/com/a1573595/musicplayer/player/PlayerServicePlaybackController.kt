package com.a1573595.musicplayer.player

import com.a1573595.musicplayer.domain.player.PlaybackController

class PlayerServicePlaybackController(
    private val service: PlayerService
) : PlaybackController {
    override var isRepeat: Boolean
        get() = service.isRepeat
        set(value) {
            service.isRepeat = value
        }

    override var isRandom: Boolean
        get() = service.isRandom
        set(value) {
            service.isRandom = value
        }

    override suspend fun readSong() {
        service.readSong()
    }

    override fun getSongList() = service.getSongList()

    override fun getSong() = service.getSong()

    override fun isPlaying() = service.isPlaying()

    override fun getProgress() = service.getProgress()

    override fun play() = service.play()

    override fun play(position: Int) = service.play(position)

    override fun pause() = service.pause()

    override fun seekTo(progress: Int) = service.seekTo(progress)

    override fun skipToNext() = service.skipToNext()

    override fun skipToPrevious() = service.skipToPrevious()
}
