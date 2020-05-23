package com.a1573595.musicplayer.player

import android.media.MediaPlayer
import java.io.FileDescriptor
import java.util.*

class PlayerManager : Observable() {
    companion object {
        const val ACTION_COMPLETE = "action.COMPLETE"
        const val ACTION_PLAY = "action.PLAY"
        const val ACTION_PAUSE = "action.PAUSE"
        const val ACTION_STOP = "action.STOP"
    }

    private val mediaPlayer: MediaPlayer = MediaPlayer()

    private var playerProgress: Int = 0    // player progress

    init {
        setListen()
    }

    fun setChangedNotify(res: Any) {
        setChanged()
        notifyObservers(res)
    }

    fun setPlayerProgress(progress: Int) {
        playerProgress = progress * 1000
    }

    fun getPlayerProgress(): Int {
        return if (mediaPlayer.isPlaying) {
            mediaPlayer.currentPosition / 1000
        } else {
            playerProgress / 1000
        }
    }

    fun play(fd: FileDescriptor) {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.reset()

        mediaPlayer.setDataSource(fd)
        mediaPlayer.prepare()
    }

    fun seekTo(progress: Int) {
        playerProgress = progress * 1000
        mediaPlayer.seekTo(playerProgress)
    }

    fun pause() {
        playerProgress = mediaPlayer.currentPosition

        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }

        setChangedNotify(ACTION_PAUSE)
    }

    fun stop() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

    private fun setListen() {
        mediaPlayer.setOnPreparedListener {
            mediaPlayer.seekTo(playerProgress)
            mediaPlayer.start()

            setChangedNotify(ACTION_PLAY)
        }

        mediaPlayer.setOnCompletionListener {
            setChangedNotify(ACTION_COMPLETE)
        }
    }
}