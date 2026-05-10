package com.a1573595.musicplayer.ui.playsong

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.a1573595.musicplayer.ui.compose.MusicPlayerComposeTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PlaySongControlsContentComposeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun playSongControls_areDisplayedAndClickable() {
        val clicks = mutableListOf<String>()

        composeRule.setContent {
            MusicPlayerComposeTheme {
                PlaySongControlsContent(
                    state =
                        PlaySongControlsState(
                            isPlaying = false,
                            isRepeat = true,
                            isRandom = false
                        ),
                    onRepeatClick = { clicks += "repeat" },
                    onRandomClick = { clicks += "random" },
                    onBackwardClick = { clicks += "backward" },
                    onPlayPauseClick = { clicks += "play" },
                    onForwardClick = { clicks += "forward" }
                )
            }
        }

        composeRule.onNodeWithTag(PlaySongControlsContentTestTag).assertIsDisplayed()
        composeRule
            .onNodeWithTag(PlaySongRepeatButtonTestTag)
            .assertIsDisplayed()
            .assertContentDescriptionEquals("Repeat")
            .assertIsOn()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Repeat on"))
        composeRule
            .onNodeWithTag(PlaySongRandomButtonTestTag)
            .assertIsDisplayed()
            .assertContentDescriptionEquals("Random")
            .assertIsOff()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Random off"))
        composeRule.onNodeWithTag(PlaySongBackwardButtonTestTag).assertIsDisplayed()
        composeRule
            .onNodeWithTag(PlaySongBackwardButtonTestTag)
            .assertContentDescriptionEquals("Previous")
        composeRule
            .onNodeWithTag(PlaySongPlayPauseButtonTestTag)
            .assertIsDisplayed()
            .assertContentDescriptionEquals("Play")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Paused"))
        composeRule.onNodeWithTag(PlaySongForwardButtonTestTag).assertIsDisplayed()
        composeRule
            .onNodeWithTag(PlaySongForwardButtonTestTag)
            .assertContentDescriptionEquals("Next")

        composeRule.onNodeWithTag(PlaySongRepeatButtonTestTag).performClick()
        composeRule.onNodeWithTag(PlaySongRandomButtonTestTag).performClick()
        composeRule.onNodeWithTag(PlaySongBackwardButtonTestTag).performClick()
        composeRule.onNodeWithTag(PlaySongPlayPauseButtonTestTag).performClick()
        composeRule.onNodeWithTag(PlaySongForwardButtonTestTag).performClick()

        assertEquals(listOf("repeat", "random", "backward", "play", "forward"), clicks)
    }

    @Test
    fun playPauseButton_usesPauseActionWhenPlaying() {
        composeRule.setContent {
            MusicPlayerComposeTheme {
                PlaySongControlsContent(
                    state = PlaySongControlsState(isPlaying = true),
                    onRepeatClick = {},
                    onRandomClick = {},
                    onBackwardClick = {},
                    onPlayPauseClick = {},
                    onForwardClick = {}
                )
            }
        }

        composeRule
            .onNodeWithTag(PlaySongPlayPauseButtonTestTag)
            .assertIsDisplayed()
            .assertContentDescriptionEquals("Pause")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Playing"))
    }

    @Test
    fun repeatAndRandomButtons_reflectOppositeStates() {
        composeRule.setContent {
            MusicPlayerComposeTheme {
                PlaySongControlsContent(
                    state = PlaySongControlsState(isRepeat = false, isRandom = true),
                    onRepeatClick = {},
                    onRandomClick = {},
                    onBackwardClick = {},
                    onPlayPauseClick = {},
                    onForwardClick = {}
                )
            }
        }

        composeRule
            .onNodeWithTag(PlaySongRepeatButtonTestTag)
            .assertIsOff()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Repeat off"))
        composeRule
            .onNodeWithTag(PlaySongRandomButtonTestTag)
            .assertIsOn()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Random on"))
    }
}
