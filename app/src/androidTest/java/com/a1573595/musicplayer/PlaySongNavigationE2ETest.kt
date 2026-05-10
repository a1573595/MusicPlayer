package com.a1573595.musicplayer

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.a1573595.musicplayer.ui.songlist.SongListActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaySongNavigationE2ETest {
    @get:Rule
    val permissions: GrantPermissionRule = GrantPermissionRule.grant(*e2ePermissions())

    @Test
    fun tappingBottomPlayerWithCurrentSong_opensPlaySong() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val testAudio = TestAudioFile.insert(context)

        try {
            ActivityScenario.launch(SongListActivity::class.java).use {
                onView(isRoot()).perform(
                    waitUntil(
                        recyclerViewWithItemCountAtLeast(1),
                        timeoutMillis = 10000L
                    )
                )

                onView(withId(R.id.recyclerView))
                    .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

                onView(withId(R.id.bottomAppBar))
                    .check(matches(isDisplayed()))
                    .perform(click())

                onView(withId(R.id.seekBar)).check(matches(isDisplayed()))
            }
        } finally {
            testAudio.delete()
        }
    }
}
