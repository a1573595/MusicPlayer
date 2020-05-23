package com.a1573595.musicplayer.songList

import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.putAll
import androidx.recyclerview.widget.RecyclerView
import com.a1573595.musicplayer.R
import com.a1573595.musicplayer.model.Song
import com.a1573595.musicplayer.model.TimeUtil
import kotlinx.android.synthetic.main.adapter_song_list.view.*

class SongListAdapter : RecyclerView.Adapter<SongListAdapter.SongHolder>() {
    private val filteredList: SparseArray<Song> = SparseArray()

    private var listener: SongClickListener? = null

    interface SongClickListener {
        fun onSongClick(position: Int)
    }

    inner class SongHolder(v: View) : RecyclerView.ViewHolder(v) {
        init {
            itemView.setOnClickListener {
                val position = filteredList.keyAt(adapterPosition)
                listener?.onSongClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.adapter_song_list, parent, false)
        return SongHolder(view)
    }

    override fun getItemCount(): Int = filteredList.size()

    override fun onBindViewHolder(holder: SongHolder, position: Int) {
        val song: Song = filteredList.valueAt(position)

        holder.itemView.tv_name.text = song.name
        holder.itemView.tv_artist.text = song.author
        holder.itemView.tv_duration.text = TimeUtil.timeMillisToTime(song.duration)
    }

    fun setSongClickListener(listener: SongClickListener) {
        this.listener = listener
    }

    fun putSong(list: SparseArray<Song>) {
        filteredList.clear()
        filteredList.putAll(list)

        notifyDataSetChanged()
    }

    fun removeSong() {

    }
}