package com.a1573595.musicplayer.domain.song

import com.a1573595.musicplayer.model.Song
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FilterSongsUseCaseTest {
    private val filterSongs = FilterSongsUseCase()

    private val songs = listOf(
        Song(id = "1", name = "Hello", author = "Adele", duration = 1000),
        Song(id = "2", name = "Beat It", author = "Michael Jackson", duration = 2000),
        Song(id = "3", name = "Nothing Else Matters", author = "Metallica", duration = 3000)
    )

    @Test
    fun emptyQuery_returnsAllSongsWithOriginalPositions() {
        val result = filterSongs(songs, "")

        assertThat(result.songs).containsExactlyElementsIn(songs).inOrder()
        assertThat(result.originalPositions).containsExactly(0, 1, 2).inOrder()
    }

    @Test
    fun query_matchesSongNameIgnoringCase() {
        val result = filterSongs(songs, "beat")

        assertThat(result.songs).containsExactly(songs[1])
        assertThat(result.originalPositions).containsExactly(1)
    }

    @Test
    fun query_matchesAuthorIgnoringCase() {
        val result = filterSongs(songs, "metal")

        assertThat(result.songs).containsExactly(songs[2])
        assertThat(result.originalPositionAt(0)).isEqualTo(2)
    }

    @Test
    fun originalPositionAt_returnsNullForInvalidFilteredIndex() {
        val result = filterSongs(songs, "hello")

        assertThat(result.originalPositionAt(1)).isNull()
    }
}
