package com.a1573595.musicplayer

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

fun waitUntil(
    matcher: Matcher<View>,
    timeoutMillis: Long = 5000L
): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> = isRoot()

        override fun getDescription(): String = "wait until $matcher is visible in hierarchy"

        override fun perform(uiController: UiController, view: View) {
            val deadline = System.currentTimeMillis() + timeoutMillis
            do {
                if (view.findMatchingView(matcher) != null) {
                    return
                }

                uiController.loopMainThreadForAtLeast(50L)
            } while (System.currentTimeMillis() < deadline)

            throw AssertionError("Timed out waiting for $matcher")
        }
    }
}

private fun View.findMatchingView(matcher: Matcher<View>): View? {
    if (matcher.matches(this)) return this

    if (this is android.view.ViewGroup) {
        for (index in 0 until childCount) {
            val match = getChildAt(index).findMatchingView(matcher)
            if (match != null) return match
        }
    }

    return null
}

fun recyclerViewWithItemCountAtLeast(minItemCount: Int): Matcher<View> {
    return object : TypeSafeMatcher<View>(RecyclerView::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("RecyclerView with item count at least $minItemCount")
        }

        override fun matchesSafely(item: View): Boolean {
            val recyclerView = item as? RecyclerView ?: return false
            return (recyclerView.adapter?.itemCount ?: 0) >= minItemCount
        }
    }
}
