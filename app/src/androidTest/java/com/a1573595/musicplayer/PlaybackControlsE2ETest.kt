package com.a1573595.musicplayer

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.a1573595.musicplayer.playSong.PlaySongActivity
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
}
