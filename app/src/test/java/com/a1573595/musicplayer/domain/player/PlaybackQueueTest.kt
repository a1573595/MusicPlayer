package com.a1573595.musicplayer.domain.player

import com.a1573595.musicplayer.domain.song.Song
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PlaybackQueueTest {
    private val songs = listOf(
        Song(id = "1", name = "One", author = "A", duration = 1000),
        Song(id = "2", name = "Two", author = "B", duration = 2000),
        Song(id = "3", name = "Three", author = "C", duration = 3000)
    )

    @Test
    fun play_returnsNullForEmptyQueue() {
        val queue = PlaybackQueue()

        assertThat(queue.play()).isNull()
        assertThat(queue.currentIndex).isEqualTo(0)
    }

    @Test
    fun next_wrapsToFirstSong() {
        val queue = PlaybackQueue(songs = songs, currentIndex = 2)

        assertThat(queue.next()).isEqualTo(songs[0])
        assertThat(queue.currentIndex).isEqualTo(0)
    }

    @Test
    fun previous_wrapsToLastSong() {
        val queue = PlaybackQueue(songs = songs, currentIndex = 0)

        assertThat(queue.previous()).isEqualTo(songs[2])
        assertThat(queue.currentIndex).isEqualTo(2)
    }

    @Test
    fun complete_replaysCurrentSongWhenRepeatIsEnabled() {
        val queue = PlaybackQueue(songs = songs, currentIndex = 1, isRepeat = true)

        assertThat(queue.complete()).isEqualTo(songs[1])
        assertThat(queue.currentIndex).isEqualTo(1)
    }

    @Test
    fun randomNext_usesInjectedRandomIndex() {
        val queue = PlaybackQueue(
            songs = songs,
            currentIndex = 0,
            isRandom = true,
            randomIndex = { 2 }
        )

        assertThat(queue.next()).isEqualTo(songs[2])
        assertThat(queue.currentIndex).isEqualTo(2)
    }

    @Test
    fun replaceSongs_keepsCurrentIndexWhenStillInRange() {
        val queue = PlaybackQueue(songs = songs, currentIndex = 1)

        queue.replaceSongs(listOf(songs[0], songs[1]))

        assertThat(queue.currentSong()).isEqualTo(songs[1])
        assertThat(queue.songs()).containsExactly(songs[0], songs[1]).inOrder()
    }

    @Test
    fun replaceSongs_normalizesCurrentIndexWhenNewListIsShorter() {
        val queue = PlaybackQueue(songs = songs, currentIndex = 2)

        queue.replaceSongs(listOf(songs[0], songs[1]))

        assertThat(queue.currentSong()).isEqualTo(songs[0])
        assertThat(queue.currentIndex).isEqualTo(0)
    }

    @Test
    fun addIfAbsent_addsNewSongOnlyOnce() {
        val queue = PlaybackQueue(songs = listOf(songs[0]))

        assertThat(queue.addIfAbsent(songs[1])).isTrue()
        assertThat(queue.addIfAbsent(songs[1])).isFalse()
        assertThat(queue.songs()).containsExactly(songs[0], songs[1]).inOrder()
    }

    @Test
    fun removeCurrent_movesToNextSongAtSameIndex() {
        val queue = PlaybackQueue(songs = songs, currentIndex = 1)

        assertThat(queue.removeCurrent()).isEqualTo(songs[2])
        assertThat(queue.currentIndex).isEqualTo(1)
        assertThat(queue.songs()).containsExactly(songs[0], songs[2]).inOrder()
    }

    @Test
    fun removeCurrent_returnsNullWhenRemovingLastSong() {
        val queue = PlaybackQueue(songs = listOf(songs[0]))

        assertThat(queue.removeCurrent()).isNull()
        assertThat(queue.currentIndex).isEqualTo(0)
        assertThat(queue.songs()).isEmpty()
    }
}
