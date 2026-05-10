package com.a1573595.musicplayer.ui.page.songlist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.a1573595.musicplayer.E2E_TEST_TIMEOUT_MILLIS
import com.a1573595.musicplayer.ui.compose.MusicPlayerComposeTheme
import org.junit.Rule
import org.junit.Test

class BottomMiniPlayerContentComposeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test(timeout = E2E_TEST_TIMEOUT_MILLIS)
    fun bottomMiniPlayerText_isDisplayed() {
        composeRule.setContent {
            MusicPlayerComposeTheme {
                BottomMiniPlayerContent(
                    state =
                        BottomMiniPlayerState(
                            songName = "Song Name",
                            artist = "Artist Name"
                        )
                )
            }
        }

        composeRule.onNodeWithTag(BottomMiniPlayerContentTestTag).assertIsDisplayed()
        composeRule
            .onNodeWithTag(BottomMiniPlayerTitleTestTag)
            .assertIsDisplayed()
            .assertTextEquals("Song Name")
        composeRule
            .onNodeWithTag(BottomMiniPlayerArtistTestTag)
            .assertIsDisplayed()
            .assertTextEquals("Artist Name")
    }
}
