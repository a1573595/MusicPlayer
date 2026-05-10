package com.a1573595.musicplayer.ui.page.songlist

import androidx.recyclerview.widget.DiffUtil
import com.a1573595.musicplayer.domain.song.Song

class SongItemCallback : DiffUtil.ItemCallback<Song>() {
    override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
        return oldItem == newItem
    }
}
