package com.a1573595.musicplayer.ui.playsong

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PlaySongControlsStateMapperTest {
    @Test
    fun defaultState_matchesCurrentXmlInitialControlState() {
        val state = PlaySongControlsStateMapper.defaultState()

        assertThat(state)
            .isEqualTo(
                PlaySongControlsState(
                    isPlaying = false,
                    isRepeat = false,
                    isRandom = false
                )
            )
    }

    @Test
    fun fromPlaybackState_mapsPlaybackFlagsToComposeState() {
        val state =
            PlaySongControlsStateMapper.fromPlaybackState(
                isPlaying = true,
                isRepeat = false,
                isRandom = true
            )

        assertThat(state)
            .isEqualTo(
                PlaySongControlsState(
                    isPlaying = true,
                    isRepeat = false,
                    isRandom = true
                )
            )
    }

    @Test
    fun withPlaying_preservesRepeatAndRandom() {
        val currentState =
            PlaySongControlsState(
                isPlaying = false,
                isRepeat = true,
                isRandom = true
            )

        val state = PlaySongControlsStateMapper.withPlaying(currentState, isPlaying = true)

        assertThat(state)
            .isEqualTo(
                PlaySongControlsState(
                    isPlaying = true,
                    isRepeat = true,
                    isRandom = true
                )
            )
    }

    @Test
    fun withRepeat_preservesPlayingAndRandom() {
        val currentState =
            PlaySongControlsState(
                isPlaying = true,
                isRepeat = false,
                isRandom = true
            )

        val state = PlaySongControlsStateMapper.withRepeat(currentState, isRepeat = true)

        assertThat(state)
            .isEqualTo(
                PlaySongControlsState(
                    isPlaying = true,
                    isRepeat = true,
                    isRandom = true
                )
            )
    }

    @Test
    fun withRandom_preservesPlayingAndRepeat() {
        val currentState =
            PlaySongControlsState(
                isPlaying = true,
                isRepeat = true,
                isRandom = false
            )

        val state = PlaySongControlsStateMapper.withRandom(currentState, isRandom = true)

        assertThat(state)
            .isEqualTo(
                PlaySongControlsState(
                    isPlaying = true,
                    isRepeat = true,
                    isRandom = true
                )
            )
    }

    @Test
    fun controlAlpha_matchesXmlImageAlphaContract() {
        assertThat(PlaySongControlsStateMapper.controlAlpha(true)).isEqualTo(255)
        assertThat(PlaySongControlsStateMapper.controlAlpha(false)).isEqualTo(80)
        assertThat(PlaySongControlsStateMapper.controlAlphaFraction(true)).isEqualTo(1f)
        assertThat(PlaySongControlsStateMapper.controlAlphaFraction(false))
            .isWithin(0.0001f)
            .of(80f / 255f)
    }
}
