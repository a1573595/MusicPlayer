package com.a1573595.musicplayer.songList

import androidx.recyclerview.widget.DiffUtil
import com.a1573595.musicplayer.model.Song

class SongItemCallback : DiffUtil.ItemCallback<Song>() {
    override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
        return oldItem == newItem
    }
}
