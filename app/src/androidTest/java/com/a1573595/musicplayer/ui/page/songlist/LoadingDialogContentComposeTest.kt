package com.a1573595.musicplayer.ui.page.songlist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.a1573595.musicplayer.ui.compose.MusicPlayerComposeTheme
import org.junit.Rule
import org.junit.Test

class LoadingDialogContentComposeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingDialogContent_isDisplayed() {
        composeRule.setContent {
            MusicPlayerComposeTheme {
                LoadingDialogContent()
            }
        }

        composeRule.onNodeWithTag(LoadingDialogContentTestTag).assertIsDisplayed()
    }
}
