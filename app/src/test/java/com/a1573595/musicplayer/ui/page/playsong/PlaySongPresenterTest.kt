package com.a1573595.musicplayer.ui.page.playsong

import android.content.Context
import android.view.View
import com.a1573595.musicplayer.FakePlaybackController
import com.a1573595.musicplayer.domain.song.Song
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PlaySongPresenterTest {
    private val song = Song(id = "1", name = "Hello", author = "Adele", duration = 1000)

    @Test
    fun setPlaybackController_fetchesCurrentSongState() {
        val view = RecordingPlaySongView()
        val player = FakePlaybackController(
            songs = listOf(song),
            currentSong = song,
            playing = true,
            progress = 7
        )

        PlaySongPresenter(view).setPlaybackController(player)

        assertThat(view.updatedSongState).isEqualTo(Triple(song, true, 7))
        assertThat(view.repeatStates).containsExactly(false)
        assertThat(view.randomStates).containsExactly(false)
    }

    @Test
    fun setPlaybackController_fetchesCurrentPlaybackModes() {
        val view = RecordingPlaySongView()
        val player = FakePlaybackController(songs = listOf(song), currentSong = song).apply {
            isRepeat = true
            isRandom = true
        }

        PlaySongPresenter(view).setPlaybackController(player)

        assertThat(view.repeatStates).containsExactly(true)
        assertThat(view.randomStates).containsExactly(true)
    }

    @Test
    fun updateRepeatAndRandom_togglePlaybackMode() {
        val view = RecordingPlaySongView()
        val player = FakePlaybackController(songs = listOf(song), currentSong = song)
        val presenter = PlaySongPresenter(view)
        presenter.setPlaybackController(player)

        assertThat(presenter.updateRepeat()).isTrue()
        assertThat(player.isRepeat).isTrue()
        assertThat(presenter.updateRandom()).isTrue()
        assertThat(player.isRandom).isTrue()
    }

    @Test
    fun controls_delegateToPlaybackController() {
        val view = RecordingPlaySongView()
        val player = FakePlaybackController(songs = listOf(song), currentSong = song)
        val presenter = PlaySongPresenter(view)
        presenter.setPlaybackController(player)

        presenter.onSongPlay()
        presenter.onSongPlay()
        presenter.seekTo(10)
        presenter.skipToNext()
        presenter.skipToPrevious()

        assertThat(player.playCalls).isEqualTo(1)
        assertThat(player.pauseCalls).isEqualTo(1)
        assertThat(player.seekProgress).isEqualTo(10)
        assertThat(player.skipToNextCalls).isEqualTo(1)
        assertThat(player.skipToPreviousCalls).isEqualTo(1)
    }

    private class RecordingPlaySongView : PlaySongView {
        var updatedSongState: Triple<Song, Boolean, Int>? = null
        val repeatStates = mutableListOf<Boolean>()
        val randomStates = mutableListOf<Boolean>()

        override fun updateSongState(song: Song, isPlaying: Boolean, progress: Int) {
            updatedSongState = Triple(song, isPlaying, progress)
        }

        override fun showRepeat(isRepeat: Boolean) {
            repeatStates += isRepeat
        }

        override fun showRandom(isRandom: Boolean) {
            randomStates += isRandom
        }

        override fun isActive(): Boolean = true

        override fun context(): Context {
            error("Context should not be used in this test")
        }

        override fun showToast(msg: String) = Unit

        override fun showSnackBar(v: View, msg: String) = Unit
    }
}
