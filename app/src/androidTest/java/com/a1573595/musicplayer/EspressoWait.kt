package com.a1573595.musicplayer

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.os.SystemClock
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

const val E2E_WAIT_TIMEOUT_MILLIS = 10_000L
const val E2E_TEST_TIMEOUT_MILLIS = 30_000L

private const val E2E_POLL_INTERVAL_MILLIS = 50L

fun waitUntil(
    matcher: Matcher<View>,
    timeoutMillis: Long = E2E_WAIT_TIMEOUT_MILLIS
): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> = isRoot()

        override fun getDescription(): String = "wait until $matcher is visible in hierarchy"

        override fun perform(uiController: UiController, view: View) {
            val deadline = SystemClock.elapsedRealtime() + timeoutMillis
            do {
                if (view.findMatchingView(matcher) != null) {
                    return
                }

                uiController.loopMainThreadForAtLeast(E2E_POLL_INTERVAL_MILLIS)
            } while (SystemClock.elapsedRealtime() < deadline)

            throw AssertionError("Timed out waiting for $matcher")
        }
    }
}

fun <T : Activity> ActivityScenario<T>.waitUntil(
    timeoutMillis: Long = E2E_WAIT_TIMEOUT_MILLIS,
    description: String = "activity condition",
    condition: (T) -> Boolean
) {
    val deadline = SystemClock.elapsedRealtime() + timeoutMillis

    do {
        var matched = false
        onActivity { activity ->
            matched = condition(activity)
        }

        if (matched) return
        SystemClock.sleep(E2E_POLL_INTERVAL_MILLIS)
    } while (SystemClock.elapsedRealtime() < deadline)

    throw AssertionError("Timed out waiting for $description")
}

fun <T : Activity> T.waitUntil(
    instrumentation: Instrumentation,
    timeoutMillis: Long = E2E_WAIT_TIMEOUT_MILLIS,
    description: String = "activity condition",
    condition: (T) -> Boolean
) {
    val deadline = SystemClock.elapsedRealtime() + timeoutMillis

    do {
        var matched = false
        instrumentation.runOnMainSync {
            matched = condition(this)
        }

        if (matched) return
        SystemClock.sleep(E2E_POLL_INTERVAL_MILLIS)
    } while (SystemClock.elapsedRealtime() < deadline)

    throw AssertionError("Timed out waiting for $description")
}

fun <T : Activity> Instrumentation.waitForActivity(
    activityClass: Class<T>,
    timeoutMillis: Long = E2E_WAIT_TIMEOUT_MILLIS,
    description: String = activityClass.simpleName,
    trigger: () -> Unit
): T {
    val monitor = addMonitor(activityClass.name, null, false)

    try {
        trigger()

        val activity = waitForMonitorWithTimeout(monitor, timeoutMillis)
            ?: throw AssertionError("Timed out waiting for $description")

        return checkNotNull(activityClass.cast(activity)) {
            "Activity $description did not match ${activityClass.name}"
        }
    } finally {
        removeMonitor(monitor)
    }
}

fun <T : Activity> Instrumentation.launchActivity(
    activityClass: Class<T>,
    timeoutMillis: Long = E2E_WAIT_TIMEOUT_MILLIS,
    description: String = activityClass.simpleName,
    configureIntent: Intent.() -> Unit = {}
): T {
    return waitForActivity(
        activityClass = activityClass,
        timeoutMillis = timeoutMillis,
        description = description
    ) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(context, activityClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            configureIntent()
        }

        context.startActivity(intent)
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
