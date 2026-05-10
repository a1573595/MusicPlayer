package com.a1573595.musicplayer.ui.playsong

data class PlaySongControlsState(
    val isPlaying: Boolean = false,
    val isRepeat: Boolean = false,
    val isRandom: Boolean = false
)

object PlaySongControlsStateMapper {
    const val ActiveControlAlpha = 255
    const val InactiveControlAlpha = 80

    fun defaultState(): PlaySongControlsState = PlaySongControlsState()

    fun fromPlaybackState(
        isPlaying: Boolean,
        isRepeat: Boolean,
        isRandom: Boolean
    ): PlaySongControlsState =
        PlaySongControlsState(
            isPlaying = isPlaying,
            isRepeat = isRepeat,
            isRandom = isRandom
        )

    fun withPlaying(
        currentState: PlaySongControlsState,
        isPlaying: Boolean
    ): PlaySongControlsState =
        currentState.copy(isPlaying = isPlaying)

    fun withRepeat(
        currentState: PlaySongControlsState,
        isRepeat: Boolean
    ): PlaySongControlsState =
        currentState.copy(isRepeat = isRepeat)

    fun withRandom(
        currentState: PlaySongControlsState,
        isRandom: Boolean
    ): PlaySongControlsState =
        currentState.copy(isRandom = isRandom)

    fun controlAlpha(isActive: Boolean): Int =
        if (isActive) ActiveControlAlpha else InactiveControlAlpha

    fun controlAlphaFraction(isActive: Boolean): Float =
        controlAlpha(isActive) / ActiveControlAlpha.toFloat()
}
