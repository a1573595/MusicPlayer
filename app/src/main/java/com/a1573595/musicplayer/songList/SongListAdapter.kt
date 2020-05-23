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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SongListAdapter : RecyclerView.Adapter<SongListAdapter.SongHolder>() {
    private val songList: SparseArray<Song> = SparseArray()

    inner class SongHolder(v: View) : RecyclerView.ViewHolder(v) {
        init {
            itemView.setOnClickListener {

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.adapter_song_list, parent, false)
        return SongHolder(view)
    }

    override fun getItemCount(): Int = songList.size()

    override fun onBindViewHolder(holder: SongHolder, position: Int) {
        val song: Song = songList.valueAt(position)

        holder.itemView.tv_name.text = song.name
        holder.itemView.tv_artist.text = song.author
        holder.itemView.tv_duration.text = TimeUtil.timeMillisToTime(song.duration)
    }

    suspend fun putSong(list: SparseArray<Song>) = withContext(Dispatchers.Main) {
        songList.clear()
        songList.putAll(list)

        notifyDataSetChanged()
    }

    fun removeSong() {

    }
}