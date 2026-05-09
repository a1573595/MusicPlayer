package com.a1573595.musicplayer.domain.song

interface SongRepository {
    suspend fun loadSongs(): List<Song>

    suspend fun findSong(id: String): Song?
}
