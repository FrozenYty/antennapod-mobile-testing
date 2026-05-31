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
 * TC-011: Browse Discovery Page
 *
 * Verifies that the Subscriptions screen (entry point for discovery
 * browsing) displays expected UI elements and supports basic navigation
 * via the bottom navigation bar.
 *
 * Adaptation: Discovery content requires network access. Tests focus on
 * UI element verification and tab navigation to the Subscriptions screen.
 *
 * @author Jianheng Sun
 */
@RunWith(AndroidJUnit4::class)
class TC011_BrowseDiscoveryTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java, false, false)

    @Test
    fun launchApp_shouldDisplayBottomNavigation() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottomNavigationView))
            .check(matches(isDisplayed()))
    }

    @Test
    fun navigateToSubscriptions_shouldDisplaySubscriptionsGrid() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_subscriptions))
            .perform(click())
        TestHelper.saveScreenshot("tc011-subscriptions-screen")
        onView(withId(R.id.subscriptions_grid))
            .check(matches(isDisplayed()))
    }

    @Test
    fun subscriptionsScreen_shouldDisplayToolbar() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_subscriptions))
            .perform(click())
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))
    }

    @Test
    fun subscriptionsScreen_shouldDisplayAddFeedButton() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_subscriptions))
            .perform(click())
        onView(withId(R.id.subscriptions_add))
            .check(matches(isDisplayed()))
    }
}
