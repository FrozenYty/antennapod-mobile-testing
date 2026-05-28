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
import de.danoeh.antennapod.utils.TestHelper
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * TC-003: Play Episode
 *
 * Verifies navigation between main tabs: subscriptions, queue,
 * inbox, episodes, and home.
 *
 * @author Tianyu Yao
 */
@RunWith(AndroidJUnit4::class)
class TC003_PlayEpisodeTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java, false, false)

    @Test
    fun navigateToSubscriptions_shouldDisplayContent() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_subscriptions))
            .perform(click())
        TestHelper.saveScreenshot("tc003-subscriptions")
        onView(withId(R.id.appbar))
            .check(matches(isDisplayed()))
    }

    @Test
    fun bottomNav_shouldAllowNavigationBetweenTabs() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_subscriptions))
            .perform(click())
        onView(withId(R.id.bottom_navigation_home))
            .perform(click())
        onView(withId(R.id.drawer_layout))
            .check(matches(isDisplayed()))
    }

    @Test
    fun navigateToQueue_shouldDisplayContent() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_queue))
            .perform(click())
        TestHelper.saveScreenshot("tc003-queue")
        onView(withId(R.id.appbar))
            .check(matches(isDisplayed()))
    }

    @Test
    fun navigateToInbox_shouldDisplayContent() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_inbox))
            .perform(click())
        onView(withId(R.id.appbar))
            .check(matches(isDisplayed()))
    }
}
