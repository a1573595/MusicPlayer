package com.a1573595.musicplayer.domain.player

import com.a1573595.musicplayer.model.Song

class PlaybackQueue(
    songs: List<Song> = emptyList(),
    currentIndex: Int = 0,
    var isRepeat: Boolean = false,
    var isRandom: Boolean = false,
    private val randomIndex: (IntRange) -> Int = { it.random() }
) {
    private var items: List<Song> = songs.toList()
    var currentIndex: Int = normalizeIndex(currentIndex)
        private set

    fun songs(): List<Song> = items

    fun currentSong(): Song? = items.getOrNull(currentIndex)

    fun replaceSongs(songs: List<Song>) {
        items = songs.toList()
        currentIndex = normalizeIndex(currentIndex)
    }

    fun addIfAbsent(song: Song): Boolean {
        if (items.contains(song)) {
            return false
        }

        items = items + song
        currentIndex = normalizeIndex(currentIndex)
        return true
    }

    fun removeCurrent(): Song? {
        if (items.isEmpty()) {
            currentIndex = 0
            return null
        }

        items = items.toMutableList().apply {
            removeAt(currentIndex)
        }
        currentIndex = normalizeIndex(currentIndex)
        return currentSong()
    }

    fun play(position: Int = currentIndex): Song? {
        if (items.isEmpty()) {
            currentIndex = 0
            return null
        }

        currentIndex = normalizeIndex(position)
        return currentSong()
    }

    fun next(): Song? {
        if (items.isEmpty()) return null
        return play(if (isRandom) randomIndex(items.indices) else currentIndex + 1)
    }

    fun previous(): Song? {
        if (items.isEmpty()) return null
        return play(if (isRandom) randomIndex(items.indices) else currentIndex - 1)
    }

    fun complete(): Song? {
        if (items.isEmpty()) return null
        return if (isRepeat) play(currentIndex) else next()
    }

    private fun normalizeIndex(index: Int): Int {
        if (items.isEmpty()) return 0
        return when {
            index >= items.size -> 0
            index < 0 -> items.lastIndex
            else -> index
        }
    }
}
