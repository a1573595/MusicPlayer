package com.a1573595.musicplayer

import android.view.View
import android.widget.ImageView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.a1573595.musicplayer.ui.playsong.PlaySongActivity
import com.a1573595.musicplayer.ui.playsong.PlaySongControlsStateMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaybackControlsE2ETest {
    @get:Rule
    val permissions: GrantPermissionRule = GrantPermissionRule.grant(*e2ePermissions())

    @get:Rule
    val scenarioRule = ActivityScenarioRule(PlaySongActivity::class.java)

    @Test
    fun playbackControls_canBeTappedWithoutCrash() {
        onView(withId(R.id.imgRepeat)).check(matches(isDisplayed())).perform(click())
        onView(withId(R.id.imgRandom)).check(matches(isDisplayed())).perform(click())
        onView(withId(R.id.imgBackward)).check(matches(isDisplayed())).perform(click())
        onView(withId(R.id.imgPlay)).check(matches(isDisplayed())).perform(click())
        onView(withId(R.id.imgForward)).check(matches(isDisplayed())).perform(click())
    }

    @Test
    fun playbackControls_matchXmlParityContract() {
        scenarioRule.scenario.onActivity { activity ->
            val repeat = activity.findViewById<ImageView>(R.id.imgRepeat)
            val random = activity.findViewById<ImageView>(R.id.imgRandom)
            val disc = activity.findViewById<View>(R.id.imgDisc)
            val backward = activity.findViewById<View>(R.id.imgBackward)
            val play = activity.findViewById<View>(R.id.imgPlay)
            val title = activity.findViewById<View>(R.id.tvName)

            activity.showRepeat(false)
            activity.showRandom(false)
            assertEquals(PlaySongControlsStateMapper.InactiveControlAlpha, repeat.imageAlpha)
            assertEquals(PlaySongControlsStateMapper.InactiveControlAlpha, random.imageAlpha)

            activity.showRepeat(true)
            activity.showRandom(true)
            assertEquals(PlaySongControlsStateMapper.ActiveControlAlpha, repeat.imageAlpha)
            assertEquals(PlaySongControlsStateMapper.ActiveControlAlpha, random.imageAlpha)

            assertEquals(activity.getString(R.string.transition_img_disc), disc.transitionName)
            assertEquals(activity.getString(R.string.transition_img_play), play.transitionName)
            assertEquals(180f, backward.rotation, 0.001f)
            assertNull(title.transitionName)
        }
    }
}
