package com.a1573595.musicplayer

import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.a1573595.musicplayer.domain.song.Song
import com.a1573595.musicplayer.ui.page.playsong.PlaySongActivity
import com.a1573595.musicplayer.ui.page.songlist.SongListActivity
import org.junit.Assert.assertTrue
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
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val songListActivity = instrumentation.launchActivity(
            activityClass = SongListActivity::class.java,
            description = "SongListActivity to launch"
        )

        try {
            songListActivity.waitUntil(
                instrumentation = instrumentation,
                description = "SongListActivity to bind player service"
            ) {
                it.findViewById<View>(R.id.bottomAppBar).hasOnClickListeners()
            }

            instrumentation.runOnMainSync {
                songListActivity.updateSongState(song, isPlaying = false)

                assertTrue(songListActivity.findViewById<View>(R.id.bottomAppBar).isShown)
                assertTrue(songListActivity.findViewById<View>(R.id.tvName).isShown)
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
                    description = "PlaySongActivity seekBar to be visible"
                ) { activity ->
                    activity.findViewById<View>(R.id.seekBar).isShown
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
        }
    }
}
