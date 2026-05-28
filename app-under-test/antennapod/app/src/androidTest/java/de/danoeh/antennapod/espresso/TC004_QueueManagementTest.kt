package de.danoeh.antennapod.espresso

import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import de.danoeh.antennapod.R
import de.danoeh.antennapod.activity.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * TC-004: Queue Management (Add / Remove / Reorder)
 *
 * Verifies the queue management UI: accessing the queue, checking
 * that the queue screen loads, and navigating between queue and other tabs.
 *
 * @author Tianyu Yao
 */
@RunWith(AndroidJUnit4::class)
class TC004_QueueManagementTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java, false, false)

    @Test
    fun navigateToQueue_shouldShowQueueScreen() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_queue))
            .perform(click())
        onView(withId(R.id.appbar))
            .check(matches(isDisplayed()))
    }

    @Test
    fun navigateFromQueueToHome_shouldReturnToHome() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_queue))
            .perform(click())
        onView(withId(R.id.bottom_navigation_home))
            .perform(click())
        onView(withId(R.id.drawer_layout))
            .check(matches(isDisplayed()))
    }

    @Test
    fun navigateToInbox_shouldShowInboxScreen() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_inbox))
            .perform(click())
        onView(withId(R.id.appbar))
            .check(matches(isDisplayed()))
    }

    @Test
    fun navigateToSubscriptions_shouldShowSubscriptionsScreen() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_subscriptions))
            .perform(click())
        onView(withId(R.id.appbar))
            .check(matches(isDisplayed()))
    }
}
