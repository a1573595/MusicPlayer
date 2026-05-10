package com.a1573595.musicplayer

import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.a1573595.musicplayer.ui.page.songlist.SongListActivity
import org.junit.Assert.assertEquals
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
                val search = activity.findViewById<EditText>(R.id.edName)
                val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerView)

                assertTrue(search.isShown)
                assertTrue(recyclerView.isShown)
                assertTrue(activity.displayedSongs().any { it.name == title })

                search.setText(title)
                assertEquals(title, search.text.toString())
            }

            activity.waitUntil(
                instrumentation = instrumentation,
                description = "SongListActivity search results to show inserted song"
            ) {
                it.displayedSongs().singleOrNull()?.name == title
            }

            instrumentation.runOnMainSync {
                val search = activity.findViewById<EditText>(R.id.edName)
                search.setText("${title}_no_match")
            }

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
}
