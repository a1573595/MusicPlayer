package com.a1573595.musicplayer.ui.page.playsong

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.tooling.preview.Preview
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
            PlaySongRepeatControl(
                checked = state.isRepeat,
                onClick = onRepeatClick,
                modifier = Modifier.size(48.dp),
                iconModifier = Modifier.size(32.dp)
            )
            PlaySongRandomControl(
                checked = state.isRandom,
                onClick = onRandomClick,
                modifier = Modifier.size(48.dp),
                iconModifier = Modifier.size(32.dp)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlaySongBackwardControl(
                onClick = onBackwardClick,
                modifier = Modifier.size(48.dp),
                iconModifier = Modifier.size(32.dp)
            )
            PlaySongPlayPauseControl(
                isPlaying = state.isPlaying,
                onClick = onPlayPauseClick,
                modifier = Modifier.size(64.dp),
                iconModifier = Modifier.size(48.dp)
            )
            PlaySongForwardControl(
                onClick = onForwardClick,
                modifier = Modifier.size(48.dp),
                iconModifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun PlaySongRepeatControl(
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier.fillMaxSize()
) {
    PlaySongToggleControlButton(
        iconRes = R.drawable.ic_repeat,
        contentDescription = stringResource(id = R.string.play_song_control_repeat),
        stateDescription =
            stringResource(
                id =
                    if (checked) {
                        R.string.play_song_control_repeat_on
                    } else {
                        R.string.play_song_control_repeat_off
                    }
            ),
        checked = checked,
        onClick = onClick,
        testTag = PlaySongRepeatButtonTestTag,
        modifier = modifier,
        iconModifier = iconModifier
    )
}

@Composable
fun PlaySongRandomControl(
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier.fillMaxSize()
) {
    PlaySongToggleControlButton(
        iconRes = R.drawable.ic_random,
        contentDescription = stringResource(id = R.string.play_song_control_random),
        stateDescription =
            stringResource(
                id =
                    if (checked) {
                        R.string.play_song_control_random_on
                    } else {
                        R.string.play_song_control_random_off
                    }
            ),
        checked = checked,
        onClick = onClick,
        testTag = PlaySongRandomButtonTestTag,
        modifier = modifier,
        iconModifier = iconModifier
    )
}

@Composable
fun PlaySongBackwardControl(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier.fillMaxSize()
) {
    PlaySongControlButton(
        iconRes = R.drawable.ic_next,
        contentDescription = stringResource(id = R.string.play_song_control_previous),
        onClick = onClick,
        testTag = PlaySongBackwardButtonTestTag,
        modifier = modifier,
        iconModifier = iconModifier.graphicsLayer(rotationZ = 180f),
        showRipple = false
    )
}

@Composable
fun PlaySongForwardControl(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier.fillMaxSize()
) {
    PlaySongControlButton(
        iconRes = R.drawable.ic_next,
        contentDescription = stringResource(id = R.string.play_song_control_next),
        onClick = onClick,
        testTag = PlaySongForwardButtonTestTag,
        modifier = modifier,
        iconModifier = iconModifier,
        showRipple = false
    )
}

@Composable
fun PlaySongPlayPauseControl(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier.fillMaxSize()
) {
    PlaySongControlButton(
        iconRes = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
        contentDescription =
            stringResource(
                id =
                    if (isPlaying) {
                        R.string.play_song_control_pause
                    } else {
                        R.string.play_song_control_play
                    }
            ),
        stateDescription =
            stringResource(
                id =
                    if (isPlaying) {
                        R.string.play_song_control_playing
                    } else {
                        R.string.play_song_control_paused
                    }
            ),
        onClick = onClick,
        testTag = PlaySongPlayPauseButtonTestTag,
        modifier = modifier,
        iconModifier = iconModifier
    )
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
    iconModifier: Modifier = Modifier
) {
    PlaySongControlButtonContainer(
        contentDescription = contentDescription,
        stateDescription = stateDescription,
        onClick = onClick,
        testTag = testTag,
        modifier = modifier,
        checked = checked,
        role = Role.Checkbox
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier =
                iconModifier
                    .alpha(PlaySongControlsStateMapper.controlAlphaFraction(checked))
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
    showRipple: Boolean = true
) {
    PlaySongControlButtonContainer(
        contentDescription = contentDescription,
        stateDescription = stateDescription,
        onClick = onClick,
        testTag = testTag,
        modifier = modifier,
        role = Role.Button,
        showRipple = showRipple
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier =
                iconModifier
                    .alpha(PlaySongControlsStateMapper.controlAlphaFraction(active))
        )
    }
}

@Composable
private fun PlaySongControlButtonContainer(
    contentDescription: String,
    stateDescription: String?,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    checked: Boolean? = null,
    role: Role,
    showRipple: Boolean = true,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val indication = if (showRipple) LocalIndication.current else null

    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = interactionSource,
                    indication = indication,
                    role = role,
                    onClick = onClick
                )
                .semantics {
                    this.contentDescription = contentDescription
                    stateDescription?.let { this.stateDescription = it }
                    checked?.let {
                        toggleableState =
                            if (it) {
                                ToggleableState.On
                            } else {
                                ToggleableState.Off
                            }
                    }
                }
                .testTag(testTag)
    ) {
        content()
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
