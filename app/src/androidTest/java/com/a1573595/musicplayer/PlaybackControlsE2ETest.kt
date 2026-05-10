package com.a1573595.musicplayer

import android.view.View
import android.widget.ImageView
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.a1573595.musicplayer.ui.page.playsong.PlaySongActivity
import com.a1573595.musicplayer.ui.page.playsong.PlaySongBackwardButtonTestTag
import com.a1573595.musicplayer.ui.page.playsong.PlaySongForwardButtonTestTag
import com.a1573595.musicplayer.ui.page.playsong.PlaySongRandomButtonTestTag
import com.a1573595.musicplayer.ui.page.playsong.PlaySongRepeatButtonTestTag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaybackControlsE2ETest {
    private val composeRule = createAndroidComposeRule<PlaySongActivity>()

    @get:Rule
    val ruleChain: RuleChain =
        RuleChain
            .outerRule(GrantPermissionRule.grant(*e2ePermissions()))
            .around(composeRule)

    @Test
    fun playbackControls_canBeTappedWithoutCrash() {
        composeRule
            .onNodeWithTag(PlaySongRepeatButtonTestTag)
            .assertIsDisplayed()
            .performClick()
        composeRule
            .onNodeWithTag(PlaySongRandomButtonTestTag)
            .assertIsDisplayed()
            .performClick()
        composeRule
            .onNodeWithTag(PlaySongBackwardButtonTestTag)
            .assertIsDisplayed()
            .performClick()
        onView(withId(R.id.imgPlay)).check(matches(isDisplayed())).perform(click())
        composeRule
            .onNodeWithTag(PlaySongForwardButtonTestTag)
            .assertIsDisplayed()
            .performClick()
    }

    @Test
    fun playbackControls_matchComposeHostParityContract() {
        composeRule.runOnUiThread {
            composeRule.activity.showRepeat(false)
            composeRule.activity.showRandom(false)
        }
        composeRule.waitForIdle()

        composeRule
            .onNodeWithTag(PlaySongRepeatButtonTestTag)
            .assertContentDescriptionEquals("Repeat")
            .assertIsOff()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Repeat off"))
        composeRule
            .onNodeWithTag(PlaySongRandomButtonTestTag)
            .assertContentDescriptionEquals("Random")
            .assertIsOff()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Random off"))

        composeRule.runOnUiThread {
            composeRule.activity.showRepeat(true)
            composeRule.activity.showRandom(true)
        }
        composeRule.waitForIdle()

        composeRule
            .onNodeWithTag(PlaySongRepeatButtonTestTag)
            .assertIsOn()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Repeat on"))
        composeRule
            .onNodeWithTag(PlaySongRandomButtonTestTag)
            .assertIsOn()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Random on"))
        composeRule
            .onNodeWithTag(PlaySongBackwardButtonTestTag)
            .assertContentDescriptionEquals("Previous")
        composeRule
            .onNodeWithTag(PlaySongForwardButtonTestTag)
            .assertContentDescriptionEquals("Next")

        composeRule.runOnIdle {
            val activity = composeRule.activity
            val disc = activity.findViewById<View>(R.id.imgDisc)
            val play = activity.findViewById<View>(R.id.imgPlay)
            val title = activity.findViewById<View>(R.id.tvName)
            val repeat = activity.findViewById<View>(R.id.imgRepeat)
            val random = activity.findViewById<View>(R.id.imgRandom)
            val backward = activity.findViewById<View>(R.id.imgBackward)
            val forward = activity.findViewById<View>(R.id.imgForward)

            assertEquals(activity.getString(R.string.transition_img_disc), disc.transitionName)
            assertEquals(activity.getString(R.string.transition_img_play), play.transitionName)
            assertNull(title.transitionName)
            assertNull(repeat.transitionName)
            assertNull(random.transitionName)
            assertNull(backward.transitionName)
            assertNull(forward.transitionName)
            assertTrue(repeat is ComposeView)
            assertTrue(random is ComposeView)
            assertTrue(backward is ComposeView)
            assertTrue(forward is ComposeView)
            assertTrue(play is ImageView)
        }
    }
}
