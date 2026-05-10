package com.a1573595.musicplayer.ui.songlist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.a1573595.musicplayer.e2ePermissions
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SongListLoadingDialogActivityTest {
    private val composeRule = createAndroidComposeRule<SongListActivityBase>()

    @get:Rule
    val ruleChain: RuleChain =
        RuleChain
            .outerRule(GrantPermissionRule.grant(*e2ePermissions()))
            .around(composeRule)

    @Test
    fun showLoading_rendersLoadingDialogContent() {
        try {
            composeRule.runOnUiThread {
                composeRule.activity.showLoading()
            }

            composeRule.waitUntil(timeoutMillis = 5_000L) {
                composeRule
                    .onAllNodesWithTag(LoadingDialogContentTestTag)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }

            composeRule
                .onNodeWithTag(LoadingDialogContentTestTag)
                .assertIsDisplayed()
        } finally {
            composeRule.runOnUiThread {
                composeRule.activity.stopLoading()
            }
        }
    }
}
