package com.a1573595.musicplayer.domain.song

data class FilteredSongs(
    val songs: List<Song>,
    val originalPositions: List<Int>
) {
    fun originalPositionAt(filteredIndex: Int): Int? = originalPositions.getOrNull(filteredIndex)
}

class FilterSongsUseCase {
    operator fun invoke(songs: List<Song>, query: String): FilteredSongs {
        val keyword = query.trim()
        if (keyword.isEmpty()) {
            return FilteredSongs(
                songs = songs,
                originalPositions = songs.indices.toList()
            )
        }

        val filteredSongs = mutableListOf<Song>()
        val originalPositions = mutableListOf<Int>()
        songs.forEachIndexed { index, song ->
            if (song.name.contains(keyword, ignoreCase = true) ||
                song.author.contains(keyword, ignoreCase = true)
            ) {
                filteredSongs += song
                originalPositions += index
            }
        }

        return FilteredSongs(filteredSongs, originalPositions)
    }
}
