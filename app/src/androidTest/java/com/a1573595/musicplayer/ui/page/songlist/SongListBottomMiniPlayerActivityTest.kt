package com.a1573595.musicplayer.ui.page.songlist

import android.graphics.Rect
import android.view.View
import androidx.compose.runtime.State
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.a1573595.musicplayer.E2E_TEST_TIMEOUT_MILLIS
import com.a1573595.musicplayer.R
import com.a1573595.musicplayer.e2ePermissions
import com.a1573595.musicplayer.domain.song.Song
import com.a1573595.musicplayer.launchActivity
import com.a1573595.musicplayer.ui.base.BasePlayerBoundActivity.Companion.EXTRA_SKIP_PLAYER_BINDING_FOR_TESTS
import com.a1573595.musicplayer.waitUntil
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SongListBottomMiniPlayerActivityTest {
    @get:Rule
    val permissions: GrantPermissionRule = GrantPermissionRule.grant(*e2ePermissions())

    @Test(timeout = E2E_TEST_TIMEOUT_MILLIS)
    fun updateSongState_keepsBottomMiniPlayerShellContract() {
        val song =
            Song(
                id = "test-song-id",
                name = "Compose Migration Song",
                author = "Compose Migration Artist",
                duration = 180_000L
            )

        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val activity = instrumentation.launchActivity(
            activityClass = SongListActivity::class.java,
            description = "SongListActivity to launch",
            configureIntent = {
                putExtra(EXTRA_SKIP_PLAYER_BINDING_FOR_TESTS, true)
            }
        )

        try {
            instrumentation.runOnMainSync {
                activity.updateSongState(song, false)

                val miniPlayerState = activity.bottomMiniPlayerState()
                assertEquals("Compose Migration Song", miniPlayerState.songName)
                assertEquals("Compose Migration Artist", miniPlayerState.artist)
            }
            activity.waitUntil(
                instrumentation = instrumentation,
                description = "bottom mini player layout to settle"
            ) { currentActivity ->
                val bottomAppBar = currentActivity.findViewById<BottomAppBar>(R.id.bottomAppBar)
                val playButton =
                    currentActivity.findViewById<FloatingActionButton>(R.id.btn_play)
                val miniPlayer = currentActivity.findViewById<View>(R.id.tvName)
                val bottomAppBarRect = Rect()
                val playButtonRect = Rect()
                val miniPlayerRect = Rect()

                val hasRects =
                    bottomAppBar.getGlobalVisibleRect(bottomAppBarRect) &&
                        playButton.getGlobalVisibleRect(playButtonRect) &&
                        miniPlayer.getGlobalVisibleRect(miniPlayerRect)

                hasRects &&
                    bottomAppBarRect.contains(miniPlayerRect) &&
                    miniPlayerRect.right < playButtonRect.left
            }

            instrumentation.runOnMainSync {
                val bottomAppBar = activity.findViewById<BottomAppBar>(R.id.bottomAppBar)
                val playButton = activity.findViewById<FloatingActionButton>(R.id.btn_play)
                val miniPlayer = activity.findViewById<View>(R.id.tvName)
                val density = activity.resources.displayMetrics.density
                val bottomAppBarRect = Rect()
                val playButtonRect = Rect()
                val miniPlayerRect = Rect()

                assertEquals(SongListActivity::class.java, activity::class.java)
                assertTrue(bottomAppBar.isShown)
                assertTrue(playButton.isShown)
                assertTrue(activity.findViewById<View>(R.id.imgDisc).isShown)
                assertTrue(miniPlayer.isShown)
                assertEquals(
                    R.id.bottomAppBar,
                    (playButton.layoutParams as CoordinatorLayout.LayoutParams).anchorId
                )
                assertEquals(BottomAppBar.FAB_ALIGNMENT_MODE_END, bottomAppBar.fabAlignmentMode)
                assertEquals(8f * density, bottomAppBar.fabCradleMargin, 0.5f)
                assertEquals(16f * density, bottomAppBar.fabCradleRoundedCornerRadius, 0.5f)
                assertEquals(8f * density, bottomAppBar.cradleVerticalOffset, 0.5f)
                assertNull(miniPlayer.transitionName)

                assertTrue(bottomAppBar.getGlobalVisibleRect(bottomAppBarRect))
                assertTrue(playButton.getGlobalVisibleRect(playButtonRect))
                assertTrue(miniPlayer.getGlobalVisibleRect(miniPlayerRect))
                assertTrue(bottomAppBarRect.contains(miniPlayerRect))
                assertTrue(miniPlayerRect.right < playButtonRect.left)
            }
        } finally {
            instrumentation.runOnMainSync {
                activity.finish()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun SongListActivity.bottomMiniPlayerState(): BottomMiniPlayerState {
        val bottomMiniPlayerStateField =
            SongListActivityBase::class.java.getDeclaredField("bottomMiniPlayerState").apply {
                isAccessible = true
            }

        return (bottomMiniPlayerStateField.get(this) as State<BottomMiniPlayerState>).value
    }
}
