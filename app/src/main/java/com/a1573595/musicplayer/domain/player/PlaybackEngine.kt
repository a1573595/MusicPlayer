package com.a1573595.musicplayer.domain.player

import java.beans.PropertyChangeListener
import java.io.FileDescriptor

interface PlaybackEngine {
    companion object {
        const val ACTION_COMPLETE = "action.COMPLETE"
        const val ACTION_PLAY = "action.PLAY"
        const val ACTION_PAUSE = "action.PAUSE"
        const val ACTION_STOP = "action.STOP"
    }

    var progress: Int

    fun addObserver(listener: PropertyChangeListener)

    fun removeObserver(listener: PropertyChangeListener)

    fun notifyChanged(event: String)

    fun play(fd: FileDescriptor): Boolean

    fun seekTo(progress: Int)

    fun pause()

    fun release()
}
