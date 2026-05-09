package com.a1573595.musicplayer.player

import android.media.MediaPlayer
import timber.log.Timber
import java.beans.PropertyChangeSupport
import java.io.FileDescriptor

class PlayerManager : PropertyChangeSupport(this) {
    companion object {
        const val ACTION_COMPLETE = "action.COMPLETE"
        const val ACTION_PLAY = "action.PLAY"
        const val ACTION_PAUSE = "action.PAUSE"
        const val ACTION_STOP = "action.STOP"
    }

    private enum class State {
        IDLE,
        PREPARING,
        PLAYING,
        PAUSED,
        RELEASED
    }

    private val mediaPlayer: MediaPlayer = MediaPlayer()
    private var state: State = State.IDLE
    private var resumePositionMs: Int = 0

    var playerProgress: Int
        get() {
            return if (state == State.PLAYING && mediaPlayer.isPlaying) {
                mediaPlayer.currentPosition / 1000
            } else {
                resumePositionMs / 1000
            }
        }
        set(value) {
            resumePositionMs = value * 1000
        }

    init {
        setListen()
    }

    fun setChangedNotify(event: String) {
        Timber.i("setChangedNotify  $event")
        firePropertyChange(event, null, event)
    }

    fun play(fd: FileDescriptor): Boolean {
        if (state == State.RELEASED) {
            return false
        }

        return try {
            if (state == State.PLAYING || state == State.PAUSED) {
                mediaPlayer.stop()
            }
            mediaPlayer.reset()
            mediaPlayer.setDataSource(fd)
            state = State.PREPARING
            mediaPlayer.prepareAsync()
            true
        } catch (e: Exception) {
            Timber.e(e)
            state = State.IDLE
            resumePositionMs = 0
            setChangedNotify(ACTION_STOP)
            false
        }
    }

    fun seekTo(progress: Int) {
        playerProgress = progress

        if (state == State.PLAYING || state == State.PAUSED) {
            try {
                mediaPlayer.seekTo(resumePositionMs)
            } catch (e: IllegalStateException) {
                Timber.e(e)
                state = State.IDLE
                resumePositionMs = 0
                setChangedNotify(ACTION_STOP)
            }
        }
    }

    fun pause() {
        if (state == State.PLAYING) {
            resumePositionMs = mediaPlayer.currentPosition

            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
            }

            state = State.PAUSED
            setChangedNotify(ACTION_PAUSE)
        } else if (state == State.PREPARING) {
            state = State.PAUSED
            setChangedNotify(ACTION_PAUSE)
        }
    }

    fun release() {
        if (state == State.RELEASED) {
            return
        }

        mediaPlayer.setOnPreparedListener(null)
        mediaPlayer.setOnCompletionListener(null)
        mediaPlayer.setOnErrorListener(null)
        mediaPlayer.release()
        state = State.RELEASED
    }

    private fun setListen() {
        mediaPlayer.setOnPreparedListener {
            if (state == State.PAUSED) {
                mediaPlayer.seekTo(resumePositionMs)
                return@setOnPreparedListener
            }

            if (state != State.PREPARING) {
                return@setOnPreparedListener
            }

            mediaPlayer.seekTo(resumePositionMs)
            mediaPlayer.start()
            state = State.PLAYING
            setChangedNotify(ACTION_PLAY)
        }

        mediaPlayer.setOnCompletionListener {
            state = State.IDLE
            resumePositionMs = 0
            setChangedNotify(ACTION_COMPLETE)
        }

        mediaPlayer.setOnErrorListener { mp, what, extra ->
            val currentPosition = runCatching { mp.currentPosition }.getOrDefault(-1)
            Timber.e("MediaPlayer error type:$what, code:$extra, currentPosition:$currentPosition")
            state = State.IDLE
            resumePositionMs = 0
            setChangedNotify(ACTION_STOP)
            return@setOnErrorListener true
        }
    }
}
