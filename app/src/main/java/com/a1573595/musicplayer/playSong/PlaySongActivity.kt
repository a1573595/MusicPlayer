package com.a1573595.musicplayer.playSong

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.ChangeBounds
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import com.a1573595.musicplayer.BaseSongActivity
import com.a1573595.musicplayer.R
import com.a1573595.musicplayer.databinding.ActivityPlaySongBinding
import com.a1573595.musicplayer.model.Song
import com.a1573595.musicplayer.model.TimeUtil
import com.a1573595.musicplayer.player.PlayerManager
import com.a1573595.musicplayer.player.PlayerService
import java.util.*

class PlaySongActivity : BaseSongActivity<PlaySongPresenter>(), PlaySongView {
    private val STATE_PLAY = intArrayOf(R.attr.state_pause)
    private val STATE_PAUSE = intArrayOf(-R.attr.state_pause)

    private lateinit var viewBinding: ActivityPlaySongBinding

    private lateinit var wheelAnimation: Animation
    private lateinit var scaleAnimation: Animation

    private val handler: Handler = Handler(Looper.getMainLooper())
    private lateinit var seekBarUpdateRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityPlaySongBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        setBackground()

        viewBinding.tvName.isSelected = true

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

        viewBinding.tvName.text = song.name
        viewBinding.tvDuration.text = TimeUtil.timeMillisToTime(song.duration)
        viewBinding.seekBar.max = (song.duration / 1000).toInt()
        viewBinding.seekBar.progress = progress
        viewBinding.tvProgress.text =
            TimeUtil.timeMillisToTime((viewBinding.seekBar.progress * 1000).toLong())
        viewBinding.imgPlay.setImageState(if (isPlaying) STATE_PLAY else STATE_PAUSE, false)

        if (isPlaying) {
            viewBinding.flDisc.startAnimation(wheelAnimation)
            handler.postDelayed(seekBarUpdateRunnable, 1000)
        } else {
            viewBinding.flDisc.clearAnimation()
        }
    }

    override fun showRepeat(isRepeat: Boolean) {
        viewBinding.imgRepeat.imageAlpha = if (isRepeat) 255 else 80
    }

    override fun showRandom(isRandom: Boolean) {
        viewBinding.imgRandom.imageAlpha = if (isRandom) 255 else 80
    }

    override fun update(o: Observable?, any: Any?) {
        when (any) {
            PlayerManager.ACTION_PLAY, PlayerManager.ACTION_PAUSE -> {
                updateState()
            }
        }
    }

    private fun setBackground() {
        viewBinding.root.background = ContextCompat.getDrawable(this, R.drawable.background_music)
        viewBinding.root.background.alpha = 30
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
            viewBinding.seekBar.progress = viewBinding.seekBar.progress + 1
            handler.postDelayed(seekBarUpdateRunnable, 1000)
        }
    }

    private fun setListen() {
        viewBinding.imgBack.setOnClickListener {
            onBackPressed()
        }

        viewBinding.imgRepeat.setOnClickListener {
            viewBinding.imgRepeat.imageAlpha = if (presenter.updateRepeat()) 255 else 80
        }

        viewBinding.imgRandom.setOnClickListener {
            viewBinding.imgRandom.imageAlpha = if (presenter.updateRandom()) 255 else 80
        }

        viewBinding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    handler.removeCallbacksAndMessages(null)
                }

                viewBinding.tvProgress.text =
                    TimeUtil.timeMillisToTime((viewBinding.seekBar.progress * 1000).toLong())
            }

            override fun onStartTrackingTouch(s: SeekBar) {}

            override fun onStopTrackingTouch(s: SeekBar) {
                handler.removeCallbacksAndMessages(null)

                presenter.seekTo(s.progress)
                viewBinding.tvProgress.text =
                    TimeUtil.timeMillisToTime((viewBinding.seekBar.progress * 1000).toLong())

                handler.postDelayed(seekBarUpdateRunnable, 1000)
            }
        })

        viewBinding.imgBackward.setOnClickListener {
            presenter.skipToPrevious()

            it.startAnimation(scaleAnimation)
        }

        viewBinding.imgPlay.setOnClickListener {
            presenter.onSongPlay()
        }

        viewBinding.imgForward.setOnClickListener {
            presenter.skipToNext()

            it.startAnimation(scaleAnimation)
        }
    }
}