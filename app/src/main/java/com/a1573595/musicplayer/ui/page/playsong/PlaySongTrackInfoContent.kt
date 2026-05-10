package com.a1573595.musicplayer.ui.page.playsong

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a1573595.musicplayer.ui.compose.MusicPlayerComposeTheme

const val PlaySongTrackInfoContentTestTag = "play_song_track_info_content"
const val PlaySongTrackTitleTextTestTag = "play_song_track_title_text"
const val PlaySongTrackProgressTextTestTag = "play_song_track_progress_text"
const val PlaySongTrackDurationTextTestTag = "play_song_track_duration_text"

data class PlaySongTrackInfoState(
    val title: String,
    val progress: String,
    val duration: String
)

@Composable
fun PlaySongTrackInfoContent(
    state: PlaySongTrackInfoState,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .testTag(PlaySongTrackInfoContentTestTag)
    ) {
        PlaySongTrackProgressText(text = state.progress)
        PlaySongTrackTitleText(
            text = state.title,
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
        )
        PlaySongTrackDurationText(text = state.duration)
    }
}

@Composable
fun PlaySongTrackTitleText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 22.sp,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier =
            modifier
                .testTag(PlaySongTrackTitleTextTestTag)
    )
}

@Composable
fun PlaySongTrackProgressText(
    text: String,
    modifier: Modifier = Modifier
) {
    PlaySongTrackTimeText(
        text = text,
        testTag = PlaySongTrackProgressTextTestTag,
        modifier = modifier
    )
}

@Composable
fun PlaySongTrackDurationText(
    text: String,
    modifier: Modifier = Modifier
) {
    PlaySongTrackTimeText(
        text = text,
        testTag = PlaySongTrackDurationTextTestTag,
        modifier = modifier
    )
}

@Composable
private fun PlaySongTrackTimeText(
    text: String,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 18.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.testTag(testTag)
    )
}

@Preview(showBackground = true)
@Composable
private fun PlaySongTrackInfoContentPreview() {
    MusicPlayerComposeTheme {
        PlaySongTrackInfoContent(
            state =
                PlaySongTrackInfoState(
                    title = "Hello",
                    progress = "00:00",
                    duration = "03:30"
                ),
            modifier = Modifier.padding(24.dp)
        )
    }
}
