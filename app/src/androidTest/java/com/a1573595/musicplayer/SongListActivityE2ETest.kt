package com.a1573595.musicplayer

import android.view.View
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

    @Test
    fun launchAndSearch_doesNotCrash() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val activity = instrumentation.launchActivity(
            activityClass = SongListActivity::class.java,
            description = "SongListActivity to launch"
        )

        try {
            activity.waitUntil(
                instrumentation = instrumentation,
                description = "SongListActivity to bind player service"
            ) {
                it.findViewById<View>(R.id.bottomAppBar).hasOnClickListeners()
            }

            instrumentation.runOnMainSync {
                val search = activity.findViewById<EditText>(R.id.edName)
                val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerView)

                assertTrue(search.isShown)
                search.setText("metal")
                assertEquals("metal", search.text.toString())
                assertTrue(recyclerView.isShown)
            }
        } finally {
            instrumentation.runOnMainSync {
                activity.finish()
            }
        }
    }
}
