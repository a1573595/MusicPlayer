package com.a1573595.musicplayer.domain.song

import com.a1573595.musicplayer.model.Song

interface SongRepository {
    suspend fun loadSongs(): List<Song>

    suspend fun findSong(id: String): Song?
}
