package com.a1573595.musicplayer

import android.app.Instrumentation
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.a1573595.musicplayer.ui.page.songlist.SongListActivity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SongListActivityE2ETest {
    @get:Rule
    val permissions: GrantPermissionRule = GrantPermissionRule.grant(*e2ePermissions())

    @Test(timeout = E2E_TEST_TIMEOUT_MILLIS)
    fun launchAndSearch_showsInsertedMediaStoreSong() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        context.stopPlayerServiceForE2E()
        val audioFile = TestAudioFile.insert(
            context = context,
            title = "E2E Song List ${System.currentTimeMillis()}"
        )
        val title = audioFile.title
        val activity = instrumentation.launchActivity(
            activityClass = SongListActivity::class.java,
            description = "SongListActivity to launch"
        )

        try {
            activity.waitUntil(
                instrumentation = instrumentation,
                description = "SongListActivity to show inserted MediaStore song"
            ) {
                it.findViewById<RecyclerView>(R.id.recyclerView).isShown &&
                    it.hasDisplayedSong(title)
            }

            instrumentation.runOnMainSync {
                val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerView)
                val searchBar = activity.findViewById<View>(R.id.searchBar)

                assertTrue(recyclerView.isShown)
                assertTrue(searchBar.isShown)
                assertTrue(activity.displayedSongs().any { it.name == title })
            }

            instrumentation.tapSearchBarInput(activity)
            instrumentation.sendStringSync(title)

            activity.waitUntil(
                instrumentation = instrumentation,
                description = "SongListActivity search results to show inserted song"
            ) {
                it.displayedSongs().singleOrNull()?.name == title
            }

            instrumentation.sendStringSync("_no_match")

            activity.waitUntil(
                instrumentation = instrumentation,
                description = "SongListActivity search results to hide non-matching song"
            ) {
                it.displayedSongs().none { song -> song.name == title }
            }
        } finally {
            instrumentation.runOnMainSync {
                activity.finish()
            }
            context.stopPlayerServiceForE2E()
            audioFile.delete()
        }
    }

    private fun Instrumentation.tapSearchBarInput(activity: SongListActivity) {
        var x = 0f
        var y = 0f
        runOnMainSync {
            val searchBar = activity.findViewById<View>(R.id.searchBar)
            val location = IntArray(2)
            searchBar.getLocationOnScreen(location)
            x = location[0] + searchBar.width * 0.45f
            y = location[1] + searchBar.height * 0.5f
        }

        val downTime = SystemClock.uptimeMillis()
        val down = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, x, y, 0)
        val up = MotionEvent.obtain(downTime, downTime + 50L, MotionEvent.ACTION_UP, x, y, 0)

        try {
            sendPointerSync(down)
            sendPointerSync(up)
        } finally {
            down.recycle()
            up.recycle()
        }
    }
}
