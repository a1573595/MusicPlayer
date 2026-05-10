package com.a1573595.musicplayer.ui.songlist

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.a1573595.musicplayer.ui.compose.MusicPlayerComposeTheme
import org.junit.Rule
import org.junit.Test

class BottomMiniPlayerContentComposeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun bottomMiniPlayerText_isDisplayed() {
        composeRule.setContent {
            MusicPlayerComposeTheme {
                Column {
                    BottomMiniPlayerTitle(songName = "Song Name")
                    BottomMiniPlayerArtist(artist = "Artist Name")
                }
            }
        }

        composeRule.onNodeWithTag(BottomMiniPlayerTitleTestTag).assertIsDisplayed()
        composeRule.onNodeWithTag(BottomMiniPlayerArtistTestTag).assertIsDisplayed()
        composeRule.onNodeWithText("Song Name").assertIsDisplayed()
        composeRule.onNodeWithText("Artist Name").assertIsDisplayed()
    }
}
