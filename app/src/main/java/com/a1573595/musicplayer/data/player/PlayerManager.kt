package com.a1573595.musicplayer.data.player

import android.media.MediaPlayer
import com.a1573595.musicplayer.domain.player.PlaybackEngine
import com.a1573595.musicplayer.domain.player.PlaybackEngine.Companion.ACTION_COMPLETE
import com.a1573595.musicplayer.domain.player.PlaybackEngine.Companion.ACTION_PAUSE
import com.a1573595.musicplayer.domain.player.PlaybackEngine.Companion.ACTION_PLAY
import com.a1573595.musicplayer.domain.player.PlaybackEngine.Companion.ACTION_STOP
import timber.log.Timber
import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport
import java.io.FileDescriptor

class PlayerManager : PlaybackEngine {
    private enum class State {
        IDLE,
        PREPARING,
        PLAYING,
        PAUSED,
        RELEASED
    }

    private val mediaPlayer: MediaPlayer = MediaPlayer()
    private val changeSupport = PropertyChangeSupport(this)
    private var state: State = State.IDLE
    private var resumePositionMs: Int = 0

    override var progress: Int
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

    override fun addObserver(listener: PropertyChangeListener) {
        changeSupport.addPropertyChangeListener(listener)
    }

    override fun removeObserver(listener: PropertyChangeListener) {
        changeSupport.removePropertyChangeListener(listener)
    }

    override fun notifyChanged(event: String) {
        Timber.i("notifyChanged  $event")
        changeSupport.firePropertyChange(event, null, event)
    }

    override fun play(fd: FileDescriptor): Boolean {
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
            notifyChanged(ACTION_STOP)
            false
        }
    }

    override fun seekTo(progress: Int) {
        this.progress = progress

        if (state == State.PLAYING || state == State.PAUSED) {
            try {
                mediaPlayer.seekTo(resumePositionMs)
            } catch (e: IllegalStateException) {
                Timber.e(e)
                state = State.IDLE
                resumePositionMs = 0
                notifyChanged(ACTION_STOP)
            }
        }
    }

    override fun pause() {
        if (state == State.PLAYING) {
            resumePositionMs = mediaPlayer.currentPosition

            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
            }

            state = State.PAUSED
            notifyChanged(ACTION_PAUSE)
        } else if (state == State.PREPARING) {
            state = State.PAUSED
            notifyChanged(ACTION_PAUSE)
        }
    }

    override fun release() {
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
            notifyChanged(ACTION_PLAY)
        }

        mediaPlayer.setOnCompletionListener {
            state = State.IDLE
            resumePositionMs = 0
            notifyChanged(ACTION_COMPLETE)
        }

        mediaPlayer.setOnErrorListener { mp, what, extra ->
            val currentPosition = runCatching { mp.currentPosition }.getOrDefault(-1)
            Timber.e("MediaPlayer error type:$what, code:$extra, currentPosition:$currentPosition")
            state = State.IDLE
            resumePositionMs = 0
            notifyChanged(ACTION_STOP)
            return@setOnErrorListener true
        }
    }
}
