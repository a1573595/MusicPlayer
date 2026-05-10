package com.a1573595.musicplayer.ui.page.songlist

import android.content.Context
import android.view.View
import com.a1573595.musicplayer.FakePlaybackController
import com.a1573595.musicplayer.domain.song.Song
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SongListPresenterTest {
    private val songs = listOf(
        Song(id = "1", name = "Hello", author = "Adele", duration = 1000),
        Song(id = "2", name = "Beat It", author = "Michael Jackson", duration = 2000),
        Song(id = "3", name = "Nothing Else Matters", author = "Metallica", duration = 3000)
    )

    @Test
    fun setPlaybackController_loadsSongsAndRendersList() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val view = RecordingSongListView()
        val player = FakePlaybackController(songs = songs)
        val presenter = SongListPresenter(
            view = view,
            scope = TestScope(dispatcher),
            mainDispatcher = dispatcher
        )

        presenter.setPlaybackController(player)
        advanceUntilIdle()

        assertThat(player.readSongCalls).isEqualTo(1)
        assertThat(view.loadingEvents).containsExactly("show", "stop").inOrder()
        assertThat(view.renderedSongs).containsExactlyElementsIn(songs).inOrder()
        assertThat(view.updatedSongState).isEqualTo(songs.first() to false)
    }

    @Test
    fun setPlaybackController_loadsSongsWithInitialFilterKey() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val view = RecordingSongListView()
        val player = FakePlaybackController(songs = songs)
        val presenter = SongListPresenter(
            view = view,
            scope = TestScope(dispatcher),
            mainDispatcher = dispatcher
        )

        presenter.setPlaybackController(player, initialFilterKey = "metal")
        advanceUntilIdle()

        assertThat(player.readSongCalls).isEqualTo(1)
        assertThat(view.loadingEvents).containsExactly("show", "stop").inOrder()
        assertThat(view.renderedSongs).containsExactly(songs[2])
    }

    @Test
    fun filterSong_rendersFilteredSongsAndClickPlaysOriginalPosition() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val view = RecordingSongListView()
        val player = FakePlaybackController(songs = songs)
        val presenter = SongListPresenter(
            view = view,
            scope = TestScope(dispatcher),
            mainDispatcher = dispatcher
        )
        presenter.setPlaybackController(player)
        advanceUntilIdle()

        presenter.filterSong("metal")
        advanceUntilIdle()
        presenter.onSongClick(0)

        assertThat(view.renderedSongs).containsExactly(songs[2])
        assertThat(view.songClickCalls).isEqualTo(1)
        assertThat(player.playedPosition).isEqualTo(2)
    }

    @Test
    fun onSongPlay_togglesPlayPause() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val view = RecordingSongListView()
        val player = FakePlaybackController(songs = songs)
        val presenter = SongListPresenter(
            view = view,
            scope = TestScope(dispatcher),
            mainDispatcher = dispatcher
        )
        presenter.setPlaybackController(player)
        advanceUntilIdle()

        presenter.onSongPlay()
        presenter.onSongPlay()

        assertThat(player.playCalls).isEqualTo(1)
        assertThat(player.pauseCalls).isEqualTo(1)
    }

    private class RecordingSongListView : SongListView {
        val loadingEvents = mutableListOf<String>()
        val renderedSongs = mutableListOf<Song>()
        var updatedSongState: Pair<Song, Boolean>? = null
        var songClickCalls = 0

        override fun showLoading() {
            loadingEvents += "show"
        }

        override fun stopLoading() {
            loadingEvents += "stop"
        }

        override fun renderSongs(songs: List<Song>) {
            renderedSongs.clear()
            renderedSongs += songs
        }

        override fun updateSongState(song: Song, isPlaying: Boolean) {
            updatedSongState = song to isPlaying
        }

        override fun onSongClick() {
            songClickCalls++
        }

        override fun isActive(): Boolean = true

        override fun context(): Context {
            error("Context should not be used in this test")
        }

        override fun showToast(msg: String) = Unit

        override fun showSnackBar(v: View, msg: String) = Unit
    }
}
