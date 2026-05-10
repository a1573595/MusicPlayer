package com.a1573595.musicplayer.ui.page.songlist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.a1573595.musicplayer.E2E_TEST_TIMEOUT_MILLIS
import com.a1573595.musicplayer.R
import com.a1573595.musicplayer.ui.compose.MusicPlayerComposeTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SongSearchBarComposeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test(timeout = E2E_TEST_TIMEOUT_MILLIS)
    fun songSearchBar_showsHintAndHandlesInputInfoClickAndSearch() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var query by mutableStateOf("")
        var infoClickCount = 0
        var searchedQuery = ""

        composeRule.setContent {
            MusicPlayerComposeTheme {
                SongSearchBar(
                    query = query,
                    onQueryChange = { query = it },
                    onInfoClick = { infoClickCount += 1 },
                    onSearch = { searchedQuery = it }
                )
            }
        }

        composeRule.onNodeWithTag(SongSearchBarTestTag).assertIsDisplayed()
        composeRule
            .onNodeWithTag(SongSearchBarHintTestTag, useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextEquals(context.getString(R.string.search_song))
        composeRule
            .onNodeWithTag(SongSearchBarSearchIconTestTag, useUnmergedTree = true)
            .assertIsDisplayed()

        composeRule.onNodeWithTag(SongSearchBarInputTestTag).performTextInput("Hello")

        composeRule.onNodeWithTag(SongSearchBarInputTestTag).assertTextEquals("Hello")
        assertEquals("Hello", query)

        composeRule.onNodeWithTag(SongSearchBarInfoTestTag).performClick()

        composeRule
            .onNodeWithTag(SongSearchBarInfoTestTag)
            .assertContentDescriptionEquals(context.getString(R.string.song_search_bar_info))
        assertEquals(1, infoClickCount)

        composeRule.onNodeWithTag(SongSearchBarInputTestTag).performImeAction()

        assertEquals("Hello", searchedQuery)
    }
}
