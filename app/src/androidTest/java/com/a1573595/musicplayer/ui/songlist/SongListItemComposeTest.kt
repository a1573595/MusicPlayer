package com.a1573595.musicplayer.ui.songlist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.a1573595.musicplayer.domain.song.Song
import com.a1573595.musicplayer.ui.compose.MusicPlayerComposeTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SongListItemComposeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun songListItem_showsSongAndHandlesClick() {
        var clickCount = 0

        composeRule.setContent {
            MusicPlayerComposeTheme {
                SongListItem(
                    song =
                        Song(
                            id = "song-id",
                            name = "Song Name",
                            author = "Artist Name",
                            duration = 125_000L
                        ),
                    duration = "02:05",
                    onClick = { clickCount += 1 }
                )
            }
        }

        composeRule.onNodeWithTag(SongListItemTestTag).assertIsDisplayed()
        composeRule.onNodeWithText("Song Name").assertIsDisplayed()
        composeRule.onNodeWithText("Artist Name").assertIsDisplayed()
        composeRule.onNodeWithText("02:05").assertIsDisplayed()

        composeRule.onNodeWithTag(SongListItemTestTag).performClick()

        assertEquals(1, clickCount)
    }
}
