package com.a1573595.musicplayer.ui.playsong

import com.a1573595.musicplayer.ui.base.BasePresenter
import com.a1573595.musicplayer.domain.player.PlaybackController

class PlaySongPresenter(view: PlaySongView) : BasePresenter<PlaySongView>(view) {
    private lateinit var player: PlaybackController

    fun setPlaybackController(player: PlaybackController) {
        this.player = player

        fetchSongState()
    }

    fun fetchSongState() {
        player.getSong()?.let {
            view.updateSongState(it, player.isPlaying(), player.getProgress())

            view.showRepeat(player.isRepeat)
            view.showRandom(player.isRandom)
        }
    }

    fun updateRepeat(): Boolean {
        player.isRepeat = !player.isRepeat
        return player.isRepeat
    }

    fun updateRandom(): Boolean {
        player.isRandom = !player.isRandom
        return player.isRandom
    }

    fun onSongPlay() {
        if (!player.isPlaying()) {
            player.play()
        } else {
            player.pause()
        }
    }

    fun skipToNext() = player.skipToNext()

    fun skipToPrevious() = player.skipToPrevious()

    fun seekTo(duration: Int) = player.seekTo(duration)
}
