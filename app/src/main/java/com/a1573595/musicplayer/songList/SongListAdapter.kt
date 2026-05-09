package com.a1573595.musicplayer.songList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.a1573595.musicplayer.common.format.TimeFormatter
import com.a1573595.musicplayer.databinding.AdapterSongListBinding
import com.a1573595.musicplayer.model.Song

class SongListAdapter(private val onSongClick: (Int) -> Unit) :
    ListAdapter<Song, SongListAdapter.SongHolder>(SongItemCallback()) {
    inner class SongHolder(val viewBinding: AdapterSongListBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {
        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onSongClick(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongHolder {
        val viewBinding =
            AdapterSongListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: SongHolder, position: Int) {
        val song: Song = getItem(position)

        holder.viewBinding.tvName.text = song.name
        holder.viewBinding.tvArtist.text = song.author
        holder.viewBinding.tvDuration.text = TimeFormatter.timeMillisToTime(song.duration)
    }
}
