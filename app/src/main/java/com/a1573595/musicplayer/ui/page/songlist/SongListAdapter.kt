package com.a1573595.musicplayer.ui.page.songlist

import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.a1573595.musicplayer.common.format.TimeFormatter
import com.a1573595.musicplayer.domain.song.Song
import com.a1573595.musicplayer.ui.compose.MusicPlayerComposeTheme

class SongListAdapter(private val onSongClick: (Int) -> Unit) :
    ListAdapter<Song, SongListAdapter.SongHolder>(SongItemCallback()) {

    inner class SongHolder(private val composeView: ComposeView) :
        RecyclerView.ViewHolder(composeView) {
        init {
            composeView.setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
            )
        }

        fun bind(song: Song) {
            composeView.setContent {
                MusicPlayerComposeTheme {
                    SongListItem(
                        song = song,
                        duration = TimeFormatter.timeMillisToTime(song.duration),
                        onClick = {
                            val position = bindingAdapterPosition
                            if (position != RecyclerView.NO_POSITION) {
                                onSongClick(position)
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongHolder {
        val composeView =
            ComposeView(parent.context).apply {
                layoutParams =
                    RecyclerView.LayoutParams(
                        RecyclerView.LayoutParams.MATCH_PARENT,
                        RecyclerView.LayoutParams.WRAP_CONTENT
                    )
            }
        return SongHolder(composeView)
    }

    override fun onBindViewHolder(holder: SongHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
