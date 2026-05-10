package com.a1573595.musicplayer.ui.playsong

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.a1573595.musicplayer.R
import com.a1573595.musicplayer.ui.compose.MusicPlayerComposeTheme

const val PlaySongControlsContentTestTag = "play_song_controls_content"
const val PlaySongRepeatButtonTestTag = "play_song_repeat_button"
const val PlaySongRandomButtonTestTag = "play_song_random_button"
const val PlaySongBackwardButtonTestTag = "play_song_backward_button"
const val PlaySongPlayPauseButtonTestTag = "play_song_play_pause_button"
const val PlaySongForwardButtonTestTag = "play_song_forward_button"

@Composable
fun PlaySongControlsContent(
    state: PlaySongControlsState,
    onRepeatClick: () -> Unit,
    onRandomClick: () -> Unit,
    onBackwardClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onForwardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            modifier
                .fillMaxWidth()
                .testTag(PlaySongControlsContentTestTag)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            PlaySongToggleControlButton(
                iconRes = R.drawable.ic_repeat,
                contentDescription = "Repeat",
                stateDescription = if (state.isRepeat) "Repeat on" else "Repeat off",
                checked = state.isRepeat,
                onClick = onRepeatClick,
                testTag = PlaySongRepeatButtonTestTag
            )
            PlaySongToggleControlButton(
                iconRes = R.drawable.ic_random,
                contentDescription = "Random",
                stateDescription = if (state.isRandom) "Random on" else "Random off",
                checked = state.isRandom,
                onClick = onRandomClick,
                testTag = PlaySongRandomButtonTestTag
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlaySongControlButton(
                iconRes = R.drawable.ic_next,
                contentDescription = "Previous",
                onClick = onBackwardClick,
                testTag = PlaySongBackwardButtonTestTag,
                iconModifier = Modifier.graphicsLayer(rotationZ = 180f)
            )
            PlaySongControlButton(
                iconRes = if (state.isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                contentDescription = if (state.isPlaying) "Pause" else "Play",
                stateDescription = if (state.isPlaying) "Playing" else "Paused",
                onClick = onPlayPauseClick,
                testTag = PlaySongPlayPauseButtonTestTag,
                buttonSize = 64.dp,
                iconSize = 48.dp
            )
            PlaySongControlButton(
                iconRes = R.drawable.ic_next,
                contentDescription = "Next",
                onClick = onForwardClick,
                testTag = PlaySongForwardButtonTestTag
            )
        }
    }
}

@Composable
private fun PlaySongToggleControlButton(
    iconRes: Int,
    contentDescription: String,
    stateDescription: String,
    checked: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    buttonSize: Dp = 48.dp,
    iconSize: Dp = 32.dp
) {
    IconToggleButton(
        checked = checked,
        onCheckedChange = { onClick() },
        modifier =
            modifier
                .size(buttonSize)
                .alpha(PlaySongControlsStateMapper.controlAlphaFraction(checked))
                .semantics {
                    this.contentDescription = contentDescription
                    this.stateDescription = stateDescription
                }
                .testTag(testTag)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
private fun PlaySongControlButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    stateDescription: String? = null,
    active: Boolean = true,
    buttonSize: Dp = 48.dp,
    iconSize: Dp = 32.dp
) {
    IconButton(
        onClick = onClick,
        modifier =
            modifier
                .size(buttonSize)
                .alpha(PlaySongControlsStateMapper.controlAlphaFraction(active))
                .semantics {
                    this.contentDescription = contentDescription
                    stateDescription?.let { this.stateDescription = it }
                }
                .testTag(testTag)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier =
                iconModifier
                    .size(iconSize)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaySongControlsContentPreview() {
    MusicPlayerComposeTheme {
        PlaySongControlsContent(
            state = PlaySongControlsState(isPlaying = true, isRepeat = true),
            onRepeatClick = {},
            onRandomClick = {},
            onBackwardClick = {},
            onPlayPauseClick = {},
            onForwardClick = {},
            modifier = Modifier.padding(24.dp)
        )
    }
}
