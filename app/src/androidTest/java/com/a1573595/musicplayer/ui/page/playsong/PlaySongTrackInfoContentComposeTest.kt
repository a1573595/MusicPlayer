package com.a1573595.musicplayer.ui.page.playsong

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.a1573595.musicplayer.E2E_TEST_TIMEOUT_MILLIS
import com.a1573595.musicplayer.ui.compose.MusicPlayerComposeTheme
import org.junit.Rule
import org.junit.Test

class PlaySongTrackInfoContentComposeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test(timeout = E2E_TEST_TIMEOUT_MILLIS)
    fun trackInfo_displaysTitleProgressAndDuration() {
        composeRule.setContent {
            MusicPlayerComposeTheme {
                PlaySongTrackInfoContent(
                    state =
                        PlaySongTrackInfoState(
                            title = "Song Name",
                            progress = "01:23",
                            duration = "04:56"
                        )
                )
            }
        }

        composeRule.onNodeWithTag(PlaySongTrackInfoContentTestTag).assertIsDisplayed()
        composeRule
            .onNodeWithTag(PlaySongTrackTitleTextTestTag)
            .assertIsDisplayed()
            .assertTextEquals("Song Name")
        composeRule
            .onNodeWithTag(PlaySongTrackProgressTextTestTag)
            .assertIsDisplayed()
            .assertTextEquals("01:23")
        composeRule
            .onNodeWithTag(PlaySongTrackDurationTextTestTag)
            .assertIsDisplayed()
            .assertTextEquals("04:56")
    }

    @Test(timeout = E2E_TEST_TIMEOUT_MILLIS)
    fun trackInfo_updatesWhenStateChanges() {
        val state =
            mutableStateOf(
                PlaySongTrackInfoState(
                    title = "First",
                    progress = "00:00",
                    duration = "03:00"
                )
            )

        composeRule.setContent {
            MusicPlayerComposeTheme {
                PlaySongTrackInfoContent(state = state.value)
            }
        }

        composeRule
            .onNodeWithTag(PlaySongTrackTitleTextTestTag)
            .assertTextEquals("First")
        composeRule
            .onNodeWithTag(PlaySongTrackProgressTextTestTag)
            .assertTextEquals("00:00")
        composeRule
            .onNodeWithTag(PlaySongTrackDurationTextTestTag)
            .assertTextEquals("03:00")

        state.value =
            PlaySongTrackInfoState(
                title = "Second",
                progress = "01:10",
                duration = "05:20"
            )

        composeRule.waitForIdle()

        composeRule
            .onNodeWithTag(PlaySongTrackTitleTextTestTag)
            .assertTextEquals("Second")
        composeRule
            .onNodeWithTag(PlaySongTrackProgressTextTestTag)
            .assertTextEquals("01:10")
        composeRule
            .onNodeWithTag(PlaySongTrackDurationTextTestTag)
            .assertTextEquals("05:20")
    }
}
