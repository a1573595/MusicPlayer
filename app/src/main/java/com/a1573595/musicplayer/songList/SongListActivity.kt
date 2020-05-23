package com.a1573595.musicplayer.songList

import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.view.animation.LinearInterpolator
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.a1573595.musicplayer.*
import com.a1573595.musicplayer.model.Song
import com.a1573595.musicplayer.player.PlayerManager
import com.a1573595.musicplayer.player.PlayerService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_song_list.*
import kotlinx.coroutines.*
import java.util.*

class SongListActivity : BaseSongActivity<SongListPresenter>(), SongListView,
    SongListAdapter.SongClickListener {
    private val scope = MainScope()

    private lateinit var dialog: AlertDialog

    private lateinit var wheelAnimation: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_list)
        setBackground()

        initElementAnimation()
        initRecyclerView()
    }

    override fun onRestart() {
        super.onRestart()

        presenter.filterSong(ed_name.text.toString())
    }

    override fun onDestroy() {
        super.onDestroy()

        scope.cancel()
    }

    override fun playerBound(player: PlayerService) {
        presenter.setPlayerManager(player)

        setListen()
    }

    override fun updateState() {
        presenter.fetchSongState()
    }

    override fun createPresenter(): SongListPresenter = SongListPresenter(this, scope)

    override fun showLoading() {
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

    override fun stopLoading() {
        recyclerView.scheduleLayoutAnimation()

        dialog.dismiss()
    }

    override fun updateSongState(song: Song, isPlaying: Boolean) {
        tv_name.text = song.name
        tv_artist.text = song.author
        btn_play.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)

        if (isPlaying) {
            img_disc.startAnimation(wheelAnimation)
        } else {
            img_disc.clearAnimation()
        }
    }

    override fun update(o: Observable?, any: Any?) {
        when (any) {
            PlayerManager.ACTION_PLAY, PlayerManager.ACTION_PAUSE -> {
                updateState()
            }
            PlayerService.ACTION_FIND_NEW_SONG, PlayerService.ACTION_NOT_SONG_FOUND -> {
                presenter.filterSong(ed_name.text.toString())
            }
        }
    }

    override fun onSongClick(index: Int) {
        presenter.playSong(index)

        hideKeyBoard()
        bottomAppBar.performShow()
    }

    private fun setBackground() {
        root.background = ContextCompat.getDrawable(this, R.drawable.background_music)
        root.background.alpha = 30
    }

    private fun initElementAnimation() {
        wheelAnimation = AnimationUtils.loadAnimation(this, R.anim.roation_wheel)
        wheelAnimation.duration = 1000
        wheelAnimation.repeatCount = ValueAnimator.INFINITE
    }

    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = SongListAdapter()
        adapter.setSongClickListener(this)
        recyclerView.adapter = adapter
        presenter.setAdapter(adapter)

        val controller = LayoutAnimationController(
            AnimationUtils.loadAnimation(this, R.anim.fade_in_from_bottom)
        )
        controller.order = LayoutAnimationController.ORDER_NORMAL
        controller.delay = 0.3f
        recyclerView.layoutAnimation = controller
    }

    private fun setListen() {
        ed_name.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                presenter.filterSong(s.toString())
            }
        })

        img_download.setOnClickListener {

        }

        btn_play.setOnClickListener {
            presenter.onSongPlay()

            bottomAppBar.performShow()
        }

        bottomAppBar.setOnClickListener {

        }
    }

    private fun hideKeyBoard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(ed_name.windowToken, 0)
    }
}
