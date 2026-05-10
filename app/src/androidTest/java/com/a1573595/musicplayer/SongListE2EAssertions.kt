package com.a1573595.musicplayer

import android.os.SystemClock
import android.view.MotionEvent
import androidx.compose.runtime.State
import androidx.recyclerview.widget.RecyclerView
import com.a1573595.musicplayer.domain.song.Song
import com.a1573595.musicplayer.ui.page.songlist.BottomMiniPlayerState
import com.a1573595.musicplayer.ui.page.songlist.SongListActivity
import com.a1573595.musicplayer.ui.page.songlist.SongListActivityBase
import com.a1573595.musicplayer.ui.page.songlist.SongListAdapter

fun SongListActivity.displayedSongs(): List<Song> {
    val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
    val adapter = recyclerView.adapter as? SongListAdapter
        ?: error("Song list RecyclerView is not using SongListAdapter")

    return adapter.currentList.toList()
}

fun SongListActivity.indexOfDisplayedSong(title: String): Int {
    return displayedSongs().indexOfFirst { it.name == title }
}

fun SongListActivity.hasDisplayedSong(title: String): Boolean {
    return indexOfDisplayedSong(title) >= 0
}

fun SongListActivity.scrollToDisplayedSong(title: String) {
    val index = indexOfDisplayedSong(title)
    check(index >= 0) { "Song '$title' is not in displayed songs" }

    findViewById<RecyclerView>(R.id.recyclerView).scrollToPosition(index)
}

fun SongListActivity.hasAttachedDisplayedSong(title: String): Boolean {
    val index = indexOfDisplayedSong(title)
    if (index < 0) return false

    return findViewById<RecyclerView>(R.id.recyclerView)
        .findViewHolderForAdapterPosition(index)
        ?.itemView
        ?.isShown == true
}

fun SongListActivity.performDisplayedSongClick(title: String) {
    val index = indexOfDisplayedSong(title)
    check(index >= 0) { "Song '$title' is not in displayed songs" }

    val itemView = findViewById<RecyclerView>(R.id.recyclerView)
        .findViewHolderForAdapterPosition(index)
        ?.itemView
        ?: error("Song '$title' row is not attached")

    val eventTime = SystemClock.uptimeMillis()
    val x = itemView.width / 2f
    val y = itemView.height / 2f
    val down = MotionEvent.obtain(eventTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0)
    val up = MotionEvent.obtain(eventTime, eventTime + 50L, MotionEvent.ACTION_UP, x, y, 0)

    try {
        itemView.dispatchTouchEvent(down)
        itemView.dispatchTouchEvent(up)
    } finally {
        down.recycle()
        up.recycle()
    }
}

fun SongListActivity.bottomMiniPlayerSongName(): String {
    return bottomMiniPlayerState().songName
}

@Suppress("UNCHECKED_CAST")
private fun SongListActivity.bottomMiniPlayerState(): BottomMiniPlayerState {
    val field = SongListActivityBase::class.java.getDeclaredField("bottomMiniPlayerState")
    field.isAccessible = true

    return (field.get(this) as State<BottomMiniPlayerState>).value
}
