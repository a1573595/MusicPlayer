package com.a1573595.musicplayer.ui.songlist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.a1573595.musicplayer.e2ePermissions
import com.a1573595.musicplayer.domain.song.Song
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SongListBottomMiniPlayerActivityTest {
    private val composeRule = createAndroidComposeRule<SongListActivity>()

    @get:Rule
    val ruleChain: RuleChain =
        RuleChain
            .outerRule(GrantPermissionRule.grant(*e2ePermissions()))
            .around(composeRule)

    @Test
    fun updateSongState_rendersBottomMiniPlayerText() {
        val song =
            Song(
                id = "test-song-id",
                name = "Compose Migration Song",
                author = "Compose Migration Artist",
                duration = 180_000L
            )

        composeRule.runOnUiThread {
            composeRule.activity.updateSongState(song, false)
        }
        composeRule.waitForIdle()

        composeRule
            .onNodeWithTag(BottomMiniPlayerTitleTestTag)
            .assertIsDisplayed()
            .assertTextEquals("Compose Migration Song")
        composeRule
            .onNodeWithTag(BottomMiniPlayerArtistTestTag)
            .assertIsDisplayed()
            .assertTextEquals("Compose Migration Artist")
    }
}
