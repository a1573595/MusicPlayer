package com.a1573595.musicplayer.ui.page.songlist

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a1573595.musicplayer.ui.compose.MusicPlayerComposeTheme

const val BottomMiniPlayerTitleTestTag = "bottom_mini_player_title"
const val BottomMiniPlayerArtistTestTag = "bottom_mini_player_artist"
const val BottomMiniPlayerContentTestTag = "bottom_mini_player_content"

data class BottomMiniPlayerState(
    val songName: String = "",
    val artist: String = ""
) {
    val hasSong: Boolean
        get() = songName.isNotEmpty() || artist.isNotEmpty()
}

@Composable
fun BottomMiniPlayerContent(
    state: BottomMiniPlayerState,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier =
            modifier
                .fillMaxWidth()
                .testTag(BottomMiniPlayerContentTestTag)
    ) {
        BottomMiniPlayerTitle(songName = state.songName)
        BottomMiniPlayerArtist(artist = state.artist)
    }
}

@Composable
fun BottomMiniPlayerTitle(
    songName: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = songName,
        color = MaterialTheme.colorScheme.onPrimary,
        fontSize = 18.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier =
            modifier
                .fillMaxWidth()
                .basicMarquee(iterations = Int.MAX_VALUE)
                .testTag(BottomMiniPlayerTitleTestTag)
    )
}

@Composable
fun BottomMiniPlayerArtist(
    artist: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = artist,
        color = MaterialTheme.colorScheme.onPrimary,
        fontSize = 14.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier =
            modifier
                .fillMaxWidth()
                .testTag(BottomMiniPlayerArtistTestTag)
    )
}

@Preview(showBackground = true)
@Composable
private fun BottomMiniPlayerContentPreview() {
    MusicPlayerComposeTheme {
        Box(
            modifier =
                Modifier
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(8.dp)
        ) {
            BottomMiniPlayerContent(
                state =
                    BottomMiniPlayerState(
                        songName = "Hello",
                        artist = "Adele"
                    )
            )
        }
    }
}
