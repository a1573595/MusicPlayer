package com.a1573595.musicplayer.songList

import com.a1573595.musicplayer.model.Song
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SongItemCallbackTest {
    private val callback = SongItemCallback()

    @Test
    fun areItemsTheSame_comparesId() {
        val oldSong = Song(id = "1", name = "Old", author = "A", duration = 1000)
        val newSong = Song(id = "1", name = "New", author = "B", duration = 2000)

        assertThat(callback.areItemsTheSame(oldSong, newSong)).isTrue()
    }

    @Test
    fun areContentsTheSame_comparesFullSong() {
        val oldSong = Song(id = "1", name = "Same", author = "A", duration = 1000)
        val newSong = oldSong.copy(author = "B")

        assertThat(callback.areContentsTheSame(oldSong, newSong)).isFalse()
    }
}
