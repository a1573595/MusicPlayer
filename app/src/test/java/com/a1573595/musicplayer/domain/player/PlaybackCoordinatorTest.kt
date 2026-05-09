package com.a1573595.musicplayer.domain.player

import com.a1573595.musicplayer.domain.song.Song
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.beans.PropertyChangeListener
import java.io.FileDescriptor

class PlaybackCoordinatorTest {
    private val songs = listOf(
        Song(id = "1", name = "One", author = "A", duration = 1000),
        Song(id = "2", name = "Two", author = "B", duration = 2000),
        Song(id = "3", name = "Three", author = "C", duration = 3000)
    )

    @Test
    fun play_withEmptyQueue_reportsUnavailable() {
        val engine = FakePlaybackEngine()
        val callbacks = RecordingCallbacks()
        val coordinator = createCoordinator(engine = engine, callbacks = callbacks)

        coordinator.play()

        assertThat(callbacks.unavailableCalls).isEqualTo(1)
        assertThat(engine.progress).isEqualTo(0)
        assertThat(engine.playCalls).isEqualTo(0)
    }

    @Test
    fun play_withSong_startsPlaybackAndDelegatesToEngine() {
        val engine = FakePlaybackEngine(progress = 12)
        val callbacks = RecordingCallbacks()
        val coordinator = createCoordinator(
            queue = PlaybackQueue(songs = songs, currentIndex = 0),
            engine = engine,
            callbacks = callbacks
        )

        coordinator.play(position = 1)

        assertThat(coordinator.currentSong()).isEqualTo(songs[1])
        assertThat(engine.progress).isEqualTo(0)
        assertThat(engine.playCalls).isEqualTo(1)
        assertThat(callbacks.startPlaybackCalls).isEqualTo(1)
        assertThat(callbacks.unavailableCalls).isEqualTo(0)
    }

    @Test
    fun play_samePositionDoesNotResetProgress() {
        val engine = FakePlaybackEngine(progress = 12)
        val coordinator = createCoordinator(
            queue = PlaybackQueue(songs = songs, currentIndex = 1),
            engine = engine
        )

        coordinator.play(position = 1)

        assertThat(engine.progress).isEqualTo(12)
        assertThat(engine.playCalls).isEqualTo(1)
    }

    @Test
    fun skipToNext_advancesQueueAndResetsProgress() {
        val engine = FakePlaybackEngine(progress = 12)
        val coordinator = createCoordinator(
            queue = PlaybackQueue(songs = songs, currentIndex = 0),
            engine = engine
        )

        coordinator.skipToNext()

        assertThat(coordinator.currentSong()).isEqualTo(songs[1])
        assertThat(engine.progress).isEqualTo(0)
        assertThat(engine.playCalls).isEqualTo(1)
    }

    @Test
    fun skipToPrevious_wrapsQueueAndResetsProgress() {
        val engine = FakePlaybackEngine(progress = 12)
        val coordinator = createCoordinator(
            queue = PlaybackQueue(songs = songs, currentIndex = 0),
            engine = engine
        )

        coordinator.skipToPrevious()

        assertThat(coordinator.currentSong()).isEqualTo(songs[2])
        assertThat(engine.progress).isEqualTo(0)
        assertThat(engine.playCalls).isEqualTo(1)
    }

    @Test
    fun seekTo_whenPlayingDelegatesToEngine() {
        val engine = FakePlaybackEngine()
        val coordinator = createCoordinator(
            queue = PlaybackQueue(songs = songs),
            engine = engine
        )
        coordinator.onEngineEvent(PlaybackEngine.ACTION_PLAY)

        coordinator.seekTo(42)

        assertThat(engine.seekCalls).isEqualTo(1)
        assertThat(engine.seekProgress).isEqualTo(42)
    }

    @Test
    fun seekTo_whenPausedStoresProgressAndStartsPlayback() {
        val engine = FakePlaybackEngine()
        val coordinator = createCoordinator(
            queue = PlaybackQueue(songs = songs),
            engine = engine
        )

        coordinator.seekTo(42)

        assertThat(engine.progress).isEqualTo(42)
        assertThat(engine.playCalls).isEqualTo(1)
    }

    @Test
    fun pause_clearsPlayingStateAndDelegatesToEngine() {
        val engine = FakePlaybackEngine()
        val coordinator = createCoordinator(
            queue = PlaybackQueue(songs = songs),
            engine = engine
        )
        coordinator.onEngineEvent(PlaybackEngine.ACTION_PLAY)

        coordinator.pause()

        assertThat(coordinator.isPlaying).isFalse()
        assertThat(engine.pauseCalls).isEqualTo(1)
    }

    @Test
    fun enginePlayPauseEvents_updateStateAndNotifyCallback() {
        val callbacks = RecordingCallbacks()
        val coordinator = createCoordinator(callbacks = callbacks)

        coordinator.onEngineEvent(PlaybackEngine.ACTION_PLAY)
        coordinator.onEngineEvent(PlaybackEngine.ACTION_PAUSE)

        assertThat(coordinator.isPlaying).isFalse()
        assertThat(callbacks.stateChangedCalls).isEqualTo(2)
    }

    @Test
    fun complete_withRepeatReplaysCurrentSong() {
        val engine = FakePlaybackEngine(progress = 12)
        val coordinator = createCoordinator(
            queue = PlaybackQueue(songs = songs, currentIndex = 1, isRepeat = true),
            engine = engine
        )

        coordinator.onEngineEvent(PlaybackEngine.ACTION_COMPLETE)

        assertThat(coordinator.currentSong()).isEqualTo(songs[1])
        assertThat(engine.progress).isEqualTo(0)
        assertThat(engine.playCalls).isEqualTo(1)
    }

    @Test
    fun complete_withoutRepeatPlaysNextSong() {
        val coordinator = createCoordinator(
            queue = PlaybackQueue(songs = songs, currentIndex = 1)
        )

        coordinator.onEngineEvent(PlaybackEngine.ACTION_COMPLETE)

        assertThat(coordinator.currentSong()).isEqualTo(songs[2])
    }

    @Test
    fun play_whenAudioCannotOpen_removesCurrentAndTriesNextSong() {
        val engine = FakePlaybackEngine()
        val coordinator = createCoordinator(
            queue = PlaybackQueue(songs = songs, currentIndex = 0),
            engine = engine,
            audioSource = FakePlaybackAudioSource(missingSongIds = setOf("1"))
        )

        coordinator.play()

        assertThat(coordinator.songs()).containsExactly(songs[1], songs[2]).inOrder()
        assertThat(coordinator.currentSong()).isEqualTo(songs[1])
        assertThat(engine.playCalls).isEqualTo(1)
    }

    private fun createCoordinator(
        queue: PlaybackQueue = PlaybackQueue(),
        engine: FakePlaybackEngine = FakePlaybackEngine(),
        audioSource: PlaybackAudioSource = FakePlaybackAudioSource(),
        callbacks: RecordingCallbacks = RecordingCallbacks()
    ) = PlaybackCoordinator(
        queue = queue,
        engine = engine,
        audioSource = audioSource,
        onPlaybackStart = callbacks::onPlaybackStart,
        onPlaybackStateChanged = callbacks::onPlaybackStateChanged,
        onPlaybackUnavailable = callbacks::onPlaybackUnavailable
    )

    private class RecordingCallbacks {
        var startPlaybackCalls = 0
            private set
        var stateChangedCalls = 0
            private set
        var unavailableCalls = 0
            private set

        fun onPlaybackStart() {
            startPlaybackCalls++
        }

        fun onPlaybackStateChanged() {
            stateChangedCalls++
        }

        fun onPlaybackUnavailable() {
            unavailableCalls++
        }
    }

    private class FakePlaybackAudioSource(
        private val missingSongIds: Set<String> = emptySet()
    ) : PlaybackAudioSource {
        override fun open(song: Song): PlaybackAudio? {
            if (song.id in missingSongIds) {
                return null
            }

            return PlaybackAudio(FileDescriptor())
        }
    }

    private class FakePlaybackEngine(
        override var progress: Int = 0,
        private val canPlay: Boolean = true
    ) : PlaybackEngine {
        var playCalls = 0
            private set
        var pauseCalls = 0
            private set
        var seekCalls = 0
            private set
        var seekProgress: Int? = null
            private set

        override fun addObserver(listener: PropertyChangeListener) = Unit

        override fun removeObserver(listener: PropertyChangeListener) = Unit

        override fun notifyChanged(event: String) = Unit

        override fun play(fd: FileDescriptor): Boolean {
            playCalls++
            return canPlay
        }

        override fun seekTo(progress: Int) {
            seekCalls++
            seekProgress = progress
        }

        override fun pause() {
            pauseCalls++
        }

        override fun release() = Unit
    }
}
