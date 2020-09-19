package com.a1573595.musicplayer.playSong

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.transition.ChangeBounds
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import com.a1573595.musicplayer.BaseSongActivity
import com.a1573595.musicplayer.R
import com.a1573595.musicplayer.model.Song
import com.a1573595.musicplayer.model.TimeUtil
import com.a1573595.musicplayer.player.PlayerManager
import com.a1573595.musicplayer.player.PlayerService
import kotlinx.android.synthetic.main.activity_play_song.*
import java.util.*

class PlaySongActivity : BaseSongActivity<PlaySongPresenter>(), PlaySongView {
    private val STATE_PLAY = intArrayOf(R.attr.state_pause)
    private val STATE_PAUSE = intArrayOf(-R.attr.state_pause)

    private lateinit var wheelAnimation: Animation
    private lateinit var scaleAnimation: Animation

    private val handler: Handler = Handler()
    private lateinit var seekBarUpdateRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_song)
        setBackground()

        tv_name.isSelected = true

        initWindowAnimations()
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacksAndMessages(null)
    }

    override fun playerBound(player: PlayerService) {
        initElementAnimation()
        initSeekBarUpdateRunnable()

        presenter.setPlayerManager(player)

        setListen()
    }

    override fun updateState() {
        presenter.fetchSongState()
    }

    override fun createPresenter(): PlaySongPresenter = PlaySongPresenter(this)

    override fun updateSongState(song: Song, isPlaying: Boolean, progress: Int) {
        handler.removeCallbacksAndMessages(null)

        tv_name.text = song.name
        tv_duration.text = TimeUtil.timeMillisToTime(song.duration)
        seekBar.max = (song.duration / 1000).toInt()
        seekBar.progress = progress
        tv_progress.text = TimeUtil.timeMillisToTime((seekBar.progress * 1000).toLong())
        img_play.setImageState(if (isPlaying) STATE_PLAY else STATE_PAUSE, false)

        if (isPlaying) {
            fl_disc.startAnimation(wheelAnimation)
            handler.postDelayed(seekBarUpdateRunnable, 1000)
        } else {
            fl_disc.clearAnimation()
        }
    }

    override fun showRepeat(isRepeat: Boolean) {
        img_repeat.imageAlpha = if (isRepeat) 255 else 80
    }

    override fun showRandom(isRandom: Boolean) {
        img_random.imageAlpha = if (isRandom) 255 else 80
    }

    override fun update(o: Observable?, any: Any?) {
        when (any) {
            PlayerManager.ACTION_PLAY, PlayerManager.ACTION_PAUSE -> {
                updateState()
            }
        }
    }

    private fun setBackground() {
        root.background = ContextCompat.getDrawable(this, R.drawable.background_music)
        root.background.alpha = 30
    }

    private fun initWindowAnimations() {
        val enterTransition = ChangeBounds()
        enterTransition.duration = 1000
        enterTransition.interpolator = DecelerateInterpolator()
        window.sharedElementEnterTransition = enterTransition
    }

    private fun initElementAnimation() {
        wheelAnimation = AnimationUtils.loadAnimation(this, R.anim.rotation_wheel)
        wheelAnimation.duration = 1000
        wheelAnimation.repeatCount = ValueAnimator.INFINITE

        scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.zoom_in)
        scaleAnimation.duration = 200
        scaleAnimation.repeatCount = 1
        scaleAnimation.repeatMode = Animation.REVERSE
    }

    private fun initSeekBarUpdateRunnable() {
        seekBarUpdateRunnable = Runnable {
            seekBar.progress = seekBar.progress + 1
            handler.postDelayed(seekBarUpdateRunnable, 1000)
        }
    }

    private fun setListen() {
        img_back.setOnClickListener {
            onBackPressed()
        }

        img_repeat.setOnClickListener {
            img_repeat.imageAlpha = if (presenter.updateRepeat()) 255 else 80
        }

        img_random.setOnClickListener {
            img_random.imageAlpha = if (presenter.updateRandom()) 255 else 80
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    handler.removeCallbacksAndMessages(null)
                }

                tv_progress.text = TimeUtil.timeMillisToTime((seekBar.progress * 1000).toLong())
            }

            override fun onStartTrackingTouch(s: SeekBar) {}

            override fun onStopTrackingTouch(s: SeekBar) {
                handler.removeCallbacksAndMessages(null)

                presenter.seekTo(s.progress)
                tv_progress.text = TimeUtil.timeMillisToTime((seekBar.progress * 1000).toLong())

                handler.postDelayed(seekBarUpdateRunnable, 1000)
            }
        })

        img_backward.setOnClickListener {
            presenter.skipToPrevious()

            it.startAnimation(scaleAnimation)
        }

        img_play.setOnClickListener {
            presenter.onSongPlay()
        }

        img_forward.setOnClickListener {
            presenter.skipToNext()

            it.startAnimation(scaleAnimation)
        }
    }
}
