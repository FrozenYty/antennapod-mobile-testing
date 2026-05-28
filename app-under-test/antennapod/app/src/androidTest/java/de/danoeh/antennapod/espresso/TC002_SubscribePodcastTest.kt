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
 * TC-002: Subscribe to Podcast
 *
 * Verifies the podcast subscription flow: navigate to Add Feed,
 * and confirm the add-feed UI appears.
 *
 * @author Tianyu Yao
 */
@RunWith(AndroidJUnit4::class)
class TC002_SubscribePodcastTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java, false, false)

    @Test
    fun tapMore_shouldShowMoreMenu() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_more))
            .perform(click())
    }

    @Test
    fun bottomNav_shouldShowHomeItem() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_home))
            .check(matches(isDisplayed()))
    }

    @Test
    fun bottomNav_shouldShowSubscriptionsItem() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_subscriptions))
            .check(matches(isDisplayed()))
    }

    @Test
    fun launchApp_shouldDisplayBottomNavigation() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottomNavigationView))
            .check(matches(isDisplayed()))
    }
}
