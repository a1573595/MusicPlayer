package com.a1573595.musicplayer

import android.os.SystemClock
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.a1573595.musicplayer.domain.song.Song
import com.a1573595.musicplayer.ui.page.songlist.SongListActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaySongNavigationE2ETest {
    private val song =
        Song(
            id = "navigation-test-song",
            name = "Navigation Test Song",
            author = "Navigation Test Artist",
            duration = 1_000L
        )

    @get:Rule
    val permissions: GrantPermissionRule = GrantPermissionRule.grant(*e2ePermissions())

    @Test
    fun tappingBottomPlayerWithCurrentSong_opensPlaySong() {
        ActivityScenario.launch(SongListActivity::class.java).use { scenario ->
            scenario.waitUntil(timeoutMillis = 10_000L) { activity ->
                activity.findViewById<View>(R.id.bottomAppBar).hasOnClickListeners()
            }

            scenario.onActivity { activity ->
                activity.updateSongState(song, isPlaying = false)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            onView(withId(R.id.bottomAppBar)).check(matches(isDisplayed()))
            onView(withId(R.id.tvName)).check(matches(isDisplayed()))

            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.bottomAppBar).performClick()
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            onView(withId(R.id.seekBar)).check(matches(isDisplayed()))
        }
    }

    private fun ActivityScenario<SongListActivity>.waitUntil(
        timeoutMillis: Long,
        condition: (SongListActivity) -> Boolean
    ) {
        val deadline = System.currentTimeMillis() + timeoutMillis

        do {
            var matches = false
            onActivity { activity ->
                matches = condition(activity)
            }

            if (matches) return
            SystemClock.sleep(50L)
        } while (System.currentTimeMillis() < deadline)

        error("Timed out waiting for SongListActivity condition")
    }
}
