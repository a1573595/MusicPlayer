package com.a1573595.musicplayer.ui.page.songlist

import android.app.Dialog
import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.a1573595.musicplayer.R
import com.a1573595.musicplayer.e2ePermissions
import com.a1573595.musicplayer.launchActivity
import com.a1573595.musicplayer.ui.base.BasePlayerBoundActivity.Companion.EXTRA_SKIP_PLAYER_BINDING_FOR_TESTS
import com.a1573595.musicplayer.waitUntil
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SongListLoadingDialogActivityTest {
    @get:Rule
    val permissions: GrantPermissionRule = GrantPermissionRule.grant(*e2ePermissions())

    @Test
    fun showLoading_togglesLoadingDialogVisibility() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val activity = instrumentation.launchActivity(
            activityClass = SongListActivity::class.java,
            description = "SongListActivity to launch",
            configureIntent = {
                putExtra(EXTRA_SKIP_PLAYER_BINDING_FOR_TESTS, true)
            }
        )

        try {
            activity.waitUntil(
                instrumentation = instrumentation,
                description = "SongListActivity to become visible"
            ) {
                it.findViewById<View>(R.id.bottomAppBar).isShown && !it.isLoadingDialogShowing()
            }

            instrumentation.runOnMainSync {
                activity.showLoading()
            }

            activity.waitUntil(
                instrumentation = instrumentation,
                description = "loading dialog to show"
            ) {
                it.isLoadingDialogShowing()
            }

            instrumentation.runOnMainSync {
                activity.stopLoading()
            }

            activity.waitUntil(
                instrumentation = instrumentation,
                description = "loading dialog to dismiss"
            ) {
                !it.isLoadingDialogShowing()
            }
        } finally {
            instrumentation.runOnMainSync {
                activity.stopLoading()
                activity.finish()
            }
        }
    }

    private fun SongListActivity.isLoadingDialogShowing(): Boolean {
        val loadingDialogField =
            SongListActivityBase::class.java.getDeclaredField("loadingDialog").apply {
                isAccessible = true
            }

        return (loadingDialogField.get(this) as? Dialog)?.isShowing == true
    }
}
