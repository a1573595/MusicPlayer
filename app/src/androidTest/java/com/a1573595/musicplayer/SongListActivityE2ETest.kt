package com.a1573595.musicplayer

import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.a1573595.musicplayer.ui.songlist.SongListActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SongListActivityE2ETest {
    @get:Rule
    val permissions: GrantPermissionRule = GrantPermissionRule.grant(*e2ePermissions())

    @get:Rule
    val scenarioRule = ActivityScenarioRule(SongListActivity::class.java)

    @Test
    fun launchAndSearch_doesNotCrash() {
        onView(withId(R.id.edName))
            .check(matches(isDisplayed()))
            .perform(replaceText("metal"))
        closeSoftKeyboard()

        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()))
    }
}
