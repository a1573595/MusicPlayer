package com.a1573595.musicplayer.ui.page.playsong

import android.animation.ValueAnimator
import android.graphics.Point
import android.os.Bundle
import android.transition.ChangeBounds
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import com.a1573595.musicplayer.ui.base.BasePlayerBoundActivity
import com.a1573595.musicplayer.R
import com.a1573595.musicplayer.common.format.TimeFormatter
import com.a1573595.musicplayer.ui.view.FloatingAnimationView
import com.a1573595.musicplayer.databinding.ActivityPlaySongBinding
import com.a1573595.musicplayer.domain.player.PlaybackEngine
import com.a1573595.musicplayer.domain.song.Song
import com.a1573595.musicplayer.data.player.PlayerService
import com.a1573595.musicplayer.data.player.PlayerServicePlaybackController
import com.a1573595.musicplayer.ui.compose.MusicPlayerComposeTheme
import java.beans.PropertyChangeEvent

abstract class PlaySongActivityBase : BasePlayerBoundActivity<PlaySongPresenter>(), PlaySongView {
    companion object {
        private val statePlay = intArrayOf(R.attr.state_pause)
        private val statePause = intArrayOf(-R.attr.state_pause)
    }

    private lateinit var viewBinding: ActivityPlaySongBinding

    private lateinit var wheelAnimation: Animation
    private lateinit var scaleAnimation: Animation

    private var seekBarUpdateRunnable: Runnable = Runnable {}
    private val seekBarUpdateDelayMillis: Long = 1000

    private var favoriteAnimationRunnable: Runnable = Runnable {}
    private val favoriteAnimationDelayMillis: Long = 300

    private val controlsState = mutableStateOf(PlaySongControlsStateMapper.defaultState())
    private val trackInfoState =
        mutableStateOf(
            PlaySongTrackInfoState(
                title = "",
                progress = "",
                duration = ""
            )
        )
    private var isPlayerReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideStatusBar()

        viewBinding = ActivityPlaySongBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        setBackground()
        applyEdgeToEdgeInsets()

        initWindowAnimations()
        initComposeTrackInfo()
        initComposeControls()
    }

    override fun onStop() {
        super.onStop()
        viewBinding.imgFavorite.removeCallbacks(favoriteAnimationRunnable)
        viewBinding.seekBar.removeCallbacks(seekBarUpdateRunnable)
    }

    override fun playerBound(player: PlayerService) {
        initElementAnimation()
        initFavoriteRunnable()
        initSeekBarUpdateRunnable()

        presenter.setPlaybackController(PlayerServicePlaybackController(player))
        isPlayerReady = true

        setListen()
    }

    override fun updateState() {
        presenter.fetchSongState()
    }

    override fun createPresenter(): PlaySongPresenter = PlaySongPresenter(this)

    override fun updateSongState(song: Song, isPlaying: Boolean, progress: Int) {
        viewBinding.imgFavorite.removeCallbacks(favoriteAnimationRunnable)
        viewBinding.seekBar.removeCallbacks(seekBarUpdateRunnable)

        viewBinding.seekBar.max = (song.duration / 1000).toInt()
        viewBinding.seekBar.progress = progress
        updateTrackInfo(song, progress)
        applyPlayingState(isPlaying)

        if (isPlaying) {
            viewBinding.imgFavorite.post(favoriteAnimationRunnable)
            viewBinding.flDisc.startAnimation(wheelAnimation)
            viewBinding.seekBar.postDelayed(seekBarUpdateRunnable, seekBarUpdateDelayMillis)
        } else {
            viewBinding.flDisc.clearAnimation()
        }
    }

    override fun showRepeat(isRepeat: Boolean) {
        applyRepeatState(isRepeat)
    }

    override fun showRandom(isRandom: Boolean) {
        applyRandomState(isRandom)
    }

    override fun propertyChange(event: PropertyChangeEvent) {
        when (event.propertyName) {
            PlaybackEngine.ACTION_PLAY, PlaybackEngine.ACTION_PAUSE -> {
                updateState()
            }
        }
    }

    private fun hideStatusBar() {
//        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                or View.SYSTEM_UI_FLAG_FULLSCREEN
//                or View.SYSTEM_UI_FLAG_LOW_PROFILE)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        WindowInsetsControllerCompat(window, window.decorView).apply {
            // Hide the status bar
            hide(WindowInsetsCompat.Type.statusBars())
            // Allow showing the status bar with swiping from top to bottom
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun setBackground() {
        viewBinding.root.background = ContextCompat.getDrawable(this, R.drawable.background_music)
        viewBinding.root.background.alpha = 30
    }

    private fun applyEdgeToEdgeInsets() {
        val backTopMargin =
            (viewBinding.imgBack.layoutParams as ViewGroup.MarginLayoutParams).topMargin
        val playBottomMargin =
            (viewBinding.imgPlay.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin

        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.root) { _, windowInsets ->
            val insets = windowInsets.getInsetsIgnoringVisibility(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )

            viewBinding.imgBack.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = backTopMargin + insets.top
            }
            viewBinding.imgPlay.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = playBottomMargin + insets.bottom
            }

            windowInsets
        }

        ViewCompat.requestApplyInsets(viewBinding.root)
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
            updateTrackProgress()
            viewBinding.seekBar.postDelayed(seekBarUpdateRunnable, seekBarUpdateDelayMillis)
        }
    }

    private fun initFavoriteRunnable() {
        val position = IntArray(2)
        viewBinding.imgFavorite.getLocationInWindow(position)
        val startPoint = Point((position[0]), (position[1]))

        val favoriteDrawable = viewBinding.imgFavorite.drawable

        favoriteAnimationRunnable = Runnable {
            with(FloatingAnimationView(this)) {
                setImageDrawable(favoriteDrawable)
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                layoutParams = LinearLayout.LayoutParams(80, 80)
                startPosition = startPoint
                endPosition = Point(0, 0)

                viewBinding.root.addView(this)

                this.startAnimation()
            }

            viewBinding.imgFavorite.postDelayed(
                favoriteAnimationRunnable,
                favoriteAnimationDelayMillis
            )
        }
    }

    private fun setListen() {
        viewBinding.imgBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        viewBinding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewBinding.seekBar.removeCallbacks(seekBarUpdateRunnable)
                }

                updateTrackProgress()
            }

            override fun onStartTrackingTouch(s: SeekBar) = Unit

            override fun onStopTrackingTouch(s: SeekBar) {
                viewBinding.seekBar.removeCallbacks(seekBarUpdateRunnable)

                presenter.seekTo(s.progress)
                updateTrackProgress()

                viewBinding.seekBar.postDelayed(seekBarUpdateRunnable, seekBarUpdateDelayMillis)
            }
        })

        viewBinding.imgPlay.setOnClickListener {
            presenter.onSongPlay()
        }
    }

    private fun applyPlayingState(isPlaying: Boolean) {
        controlsState.value = PlaySongControlsStateMapper.withPlaying(controlsState.value, isPlaying)
        viewBinding.imgPlay.setImageState(
            if (controlsState.value.isPlaying) statePlay else statePause,
            false
        )
    }

    private fun applyRepeatState(isRepeat: Boolean) {
        controlsState.value = PlaySongControlsStateMapper.withRepeat(controlsState.value, isRepeat)
    }

    private fun applyRandomState(isRandom: Boolean) {
        controlsState.value = PlaySongControlsStateMapper.withRandom(controlsState.value, isRandom)
    }

    private fun initComposeTrackInfo() {
        listOf(
            viewBinding.tvName,
            viewBinding.tvProgress,
            viewBinding.tvDuration
        ).forEach {
            it.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        }

        viewBinding.tvName.setContent {
            MusicPlayerComposeTheme {
                PlaySongTrackTitleText(
                    text = trackInfoState.value.title,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        viewBinding.tvProgress.setContent {
            MusicPlayerComposeTheme {
                PlaySongTrackProgressText(text = trackInfoState.value.progress)
            }
        }
        viewBinding.tvDuration.setContent {
            MusicPlayerComposeTheme {
                PlaySongTrackDurationText(text = trackInfoState.value.duration)
            }
        }
    }

    private fun initComposeControls() {
        listOf(
            viewBinding.imgRepeat,
            viewBinding.imgRandom,
            viewBinding.imgBackward,
            viewBinding.imgForward
        ).forEach {
            it.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        }

        viewBinding.imgRepeat.setContent {
            MusicPlayerComposeTheme {
                PlaySongRepeatControl(
                    checked = controlsState.value.isRepeat,
                    onClick = ::onRepeatClick,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        viewBinding.imgRandom.setContent {
            MusicPlayerComposeTheme {
                PlaySongRandomControl(
                    checked = controlsState.value.isRandom,
                    onClick = ::onRandomClick,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        viewBinding.imgBackward.setContent {
            MusicPlayerComposeTheme {
                PlaySongBackwardControl(
                    onClick = ::onBackwardClick,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        viewBinding.imgForward.setContent {
            MusicPlayerComposeTheme {
                PlaySongForwardControl(
                    onClick = ::onForwardClick,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    private fun updateTrackInfo(song: Song, progress: Int) {
        trackInfoState.value =
            PlaySongTrackInfoState(
                title = song.name,
                progress = formatProgress(progress),
                duration = TimeFormatter.timeMillisToTime(song.duration)
            )
    }

    private fun updateTrackProgress() {
        trackInfoState.value =
            trackInfoState.value.copy(
                progress = formatProgress(viewBinding.seekBar.progress)
            )
    }

    private fun formatProgress(progress: Int): String =
        TimeFormatter.timeMillisToTime((progress * 1000).toLong())

    private fun onRepeatClick() {
        if (isPlayerReady) {
            applyRepeatState(presenter.updateRepeat())
        }
    }

    private fun onRandomClick() {
        if (isPlayerReady) {
            applyRandomState(presenter.updateRandom())
        }
    }

    private fun onBackwardClick() {
        if (isPlayerReady) {
            presenter.skipToPrevious()
            viewBinding.imgBackward.startAnimation(scaleAnimation)
        }
    }

    private fun onForwardClick() {
        if (isPlayerReady) {
            presenter.skipToNext()
            viewBinding.imgForward.startAnimation(scaleAnimation)
        }
    }
}
