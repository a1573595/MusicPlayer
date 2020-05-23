package com.a1573595.musicplayer.player

import android.media.MediaPlayer
import java.util.*

class PlayerManager : Observable() {
    private val mediaPlayer: MediaPlayer = MediaPlayer()

    private var playerProgress: Int = 0    // player progress

    fun getCurrentProgress(): Int {
        return if (mediaPlayer.isPlaying) {
            mediaPlayer.currentPosition / 1000
        } else {
            playerProgress / 1000
        }
    }
}