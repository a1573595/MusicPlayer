package com.a1573595.musicplayer.domain.player

import com.a1573595.musicplayer.model.Song

class PlaybackQueue(
    songs: List<Song> = emptyList(),
    currentIndex: Int = 0,
    var isRepeat: Boolean = false,
    var isRandom: Boolean = false,
    private val randomIndex: (IntRange) -> Int = { it.random() }
) {
    private var songs: List<Song> = songs.toList()
    var currentIndex: Int = normalizeIndex(currentIndex)
        private set

    fun currentSong(): Song? = songs.getOrNull(currentIndex)

    fun play(position: Int = currentIndex): Song? {
        if (songs.isEmpty()) {
            currentIndex = 0
            return null
        }

        currentIndex = normalizeIndex(position)
        return currentSong()
    }

    fun next(): Song? {
        if (songs.isEmpty()) return null
        return play(if (isRandom) randomIndex(songs.indices) else currentIndex + 1)
    }

    fun previous(): Song? {
        if (songs.isEmpty()) return null
        return play(if (isRandom) randomIndex(songs.indices) else currentIndex - 1)
    }

    fun complete(): Song? {
        if (songs.isEmpty()) return null
        return if (isRepeat) play(currentIndex) else next()
    }

    private fun normalizeIndex(index: Int): Int {
        if (songs.isEmpty()) return 0
        return when {
            index >= songs.size -> 0
            index < 0 -> songs.lastIndex
            else -> index
        }
    }
}
