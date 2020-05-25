package com.a1573595.musicplayer.songList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.a1573595.musicplayer.R
import com.a1573595.musicplayer.model.Song
import com.a1573595.musicplayer.model.TimeUtil
import kotlinx.android.synthetic.main.adapter_song_list.view.*

class SongListAdapter(private val presenter: SongListPresenter) :
    RecyclerView.Adapter<SongListAdapter.SongHolder>() {
    inner class SongHolder(v: View) : RecyclerView.ViewHolder(v) {
        init {
            itemView.setOnClickListener {
                presenter.onSongClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.adapter_song_list, parent, false)
        return SongHolder(view)
    }

    override fun getItemCount(): Int = presenter.getItemCount()

    override fun onBindViewHolder(holder: SongHolder, position: Int) {
        val song: Song = presenter.getItem(position)

        holder.itemView.tv_name.text = song.name
        holder.itemView.tv_artist.text = song.author
        holder.itemView.tv_duration.text = TimeUtil.timeMillisToTime(song.duration)
    }
}