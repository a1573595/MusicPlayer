package com.a1573595.musicplayer.songList

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.a1573595.musicplayer.*
import com.a1573595.musicplayer.model.Song
import com.a1573595.musicplayer.player.PlayerManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_song_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class SongListActivity : BaseSongActivity<SongListPresenter>(), SongListView {
    private lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_list)
    }

    override fun playerBound(player: PlayerManager) {
        initRecyclerView()

        presenter.setPlayerManager(player)
    }

    override fun updateState() {
        presenter.fetchSongState()
    }

    override fun createPresenter(): SongListPresenter = SongListPresenter(this)

    override suspend fun showLoading() = withContext(Dispatchers.Main) {
        val view = View.inflate(context(), R.layout.dialog_loading, null)
        val imgLoad = view.findViewById<View>(R.id.img_load)

        dialog = MaterialAlertDialogBuilder(context()).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setView(view)
        dialog.setCancelable(false)
        dialog.show()

        val animator = ValueAnimator.ofInt(0, 8)
        animator.duration = 750
        animator.interpolator = LinearInterpolator()
        animator.repeatCount = ValueAnimator.INFINITE

        animator.addUpdateListener {
            imgLoad.rotation = (it.animatedValue as Int).toFloat() * 45
            imgLoad.requestLayout()
        }

        animator.start()
    }

    override suspend fun stopLoading() = withContext(Dispatchers.Main) {
        recyclerView.scheduleLayoutAnimation()

        dialog.dismiss()
    }

    override fun updateSongState(song: Song, isPlaying: Boolean) {

    }

    override fun update(o: Observable?, arg: Any?) {

    }

    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = SongListAdapter()
        recyclerView.adapter = adapter
        presenter.setAdapter(adapter)

        val controller = LayoutAnimationController(
            AnimationUtils.loadAnimation(this, R.anim.fade_in_from_bottom)
        )
        controller.order = LayoutAnimationController.ORDER_NORMAL
        controller.delay = 0.3f
        recyclerView.layoutAnimation = controller
    }
}
