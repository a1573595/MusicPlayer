package com.a1573595.musicplayer.ui.page.songlist

import android.graphics.Rect
import android.view.View
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.a1573595.musicplayer.R
import com.a1573595.musicplayer.e2ePermissions
import com.a1573595.musicplayer.domain.song.Song
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
            .onNodeWithTag(BottomMiniPlayerContentTestTag)
            .assertIsDisplayed()
        composeRule
            .onNodeWithTag(BottomMiniPlayerTitleTestTag)
            .assertIsDisplayed()
            .assertTextEquals("Compose Migration Song")
        composeRule
            .onNodeWithTag(BottomMiniPlayerArtistTestTag)
            .assertIsDisplayed()
            .assertTextEquals("Compose Migration Artist")
        onView(withId(R.id.bottomAppBar)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_play)).check(matches(isDisplayed()))
        onView(withId(R.id.imgDisc)).check(matches(isDisplayed()))
        onView(withId(R.id.tvName)).check(matches(isDisplayed()))

        composeRule.runOnIdle {
            val activity = composeRule.activity
            val bottomAppBar = activity.findViewById<BottomAppBar>(R.id.bottomAppBar)
            val playButton = activity.findViewById<FloatingActionButton>(R.id.btn_play)
            val miniPlayer = activity.findViewById<View>(R.id.tvName)
            val density = activity.resources.displayMetrics.density
            val bottomAppBarRect = Rect()
            val playButtonRect = Rect()
            val miniPlayerRect = Rect()

            assertEquals(SongListActivity::class.java, activity::class.java)
            assertEquals(
                R.id.bottomAppBar,
                (playButton.layoutParams as CoordinatorLayout.LayoutParams).anchorId
            )
            assertEquals(BottomAppBar.FAB_ALIGNMENT_MODE_END, bottomAppBar.fabAlignmentMode)
            assertEquals(8f * density, bottomAppBar.fabCradleMargin, 0.5f)
            assertEquals(16f * density, bottomAppBar.fabCradleRoundedCornerRadius, 0.5f)
            assertEquals(8f * density, bottomAppBar.cradleVerticalOffset, 0.5f)
            assertNull(miniPlayer.transitionName)

            assertTrue(bottomAppBar.getGlobalVisibleRect(bottomAppBarRect))
            assertTrue(playButton.getGlobalVisibleRect(playButtonRect))
            assertTrue(miniPlayer.getGlobalVisibleRect(miniPlayerRect))
            assertTrue(bottomAppBarRect.contains(miniPlayerRect))
            assertTrue(miniPlayerRect.right < playButtonRect.left)

            val centerTolerance = 4f * density
            val bottomAppBarCenterY = (bottomAppBarRect.top + bottomAppBarRect.bottom) / 2f
            val miniPlayerCenterY = (miniPlayerRect.top + miniPlayerRect.bottom) / 2f
            assertTrue(kotlin.math.abs(bottomAppBarCenterY - miniPlayerCenterY) <= centerTolerance)
        }
    }
}
