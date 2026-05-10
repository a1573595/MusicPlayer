package com.a1573595.musicplayer

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.a1573595.musicplayer.ui.page.playsong.PlaySongActivity
import com.a1573595.musicplayer.ui.page.songlist.SongListActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaySongNavigationE2ETest {
    @get:Rule
    val permissions: GrantPermissionRule = GrantPermissionRule.grant(*e2ePermissions())

    @Test(timeout = E2E_TEST_TIMEOUT_MILLIS)
    fun tappingBottomPlayerWithCurrentSong_opensPlaySong() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        context.stopPlayerServiceForE2E()
        val audioFile = TestAudioFile.insert(
            context = context,
            title = "E2E Navigation ${System.currentTimeMillis()}"
        )
        val title = audioFile.title
        val songListActivity = instrumentation.launchActivity(
            activityClass = SongListActivity::class.java,
            description = "SongListActivity to launch"
        )

        try {
            songListActivity.waitUntil(
                instrumentation = instrumentation,
                description = "SongListActivity to show inserted MediaStore song"
            ) {
                it.findViewById<RecyclerView>(R.id.recyclerView).isShown &&
                    it.hasDisplayedSong(title)
            }

            val songIndex = songListActivity.indexOfDisplayedSong(title)
            assertTrue("Inserted song must be visible before tapping", songIndex >= 0)

            instrumentation.runOnMainSync {
                songListActivity.scrollToDisplayedSong(title)
            }

            songListActivity.waitUntil(
                instrumentation = instrumentation,
                description = "SongListActivity to attach inserted song row"
            ) {
                it.hasAttachedDisplayedSong(title)
            }

            instrumentation.runOnMainSync {
                songListActivity.performDisplayedSongClick(title)
            }

            songListActivity.waitUntil(
                instrumentation = instrumentation,
                description = "SongListActivity bottom player to show current song"
            ) {
                it.findViewById<View>(R.id.bottomAppBar).isShown &&
                    it.bottomMiniPlayerSongName() == title
            }

            instrumentation.runOnMainSync {
                assertTrue(songListActivity.findViewById<View>(R.id.bottomAppBar).isShown)
                assertTrue(songListActivity.findViewById<View>(R.id.tvName).isShown)
                assertEquals(title, songListActivity.bottomMiniPlayerSongName())
            }

            val playSongActivity =
                instrumentation.waitForActivity(
                    activityClass = PlaySongActivity::class.java,
                    description = "PlaySongActivity to launch"
                ) {
                    instrumentation.runOnMainSync {
                        songListActivity.findViewById<View>(R.id.bottomAppBar).performClick()
                    }
                }

            try {
                playSongActivity.waitUntil(
                    instrumentation = instrumentation,
                    description = "PlaySongActivity to show current song"
                ) { activity ->
                    activity.findViewById<View>(R.id.seekBar).isShown &&
                        activity.findViewById<TextView>(R.id.tvName).text.toString() == title
                }

                instrumentation.runOnMainSync {
                    assertEquals(
                        title,
                        playSongActivity.findViewById<TextView>(R.id.tvName).text.toString()
                    )
                }
            } finally {
                instrumentation.runOnMainSync {
                    playSongActivity.finish()
                }
            }
        } finally {
            instrumentation.runOnMainSync {
                songListActivity.finish()
            }
            context.stopPlayerServiceForE2E()
            audioFile.delete()
        }
    }
}
