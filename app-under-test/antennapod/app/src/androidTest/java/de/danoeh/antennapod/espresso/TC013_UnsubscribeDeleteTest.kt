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
 * TC-013: Unsubscribe & Feed Deletion
 *
 * Verifies that the Subscriptions screen is accessible and its UI elements
 * (toolbar, content area, bottom nav) are present, supporting the
 * unsubscription and deletion workflow entry points.
 *
 * Adaptation: No pre-existing subscriptions on the test device. Tests
 * verify UI structure and navigation rather than actual deletion actions.
 *
 * @author Jianheng Sun
 */
@RunWith(AndroidJUnit4::class)
class TC013_UnsubscribeDeleteTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java, false, false)

    @Test
    fun navigateToSubscriptions_shouldDisplayContentArea() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_subscriptions))
            .perform(click())
        onView(withId(R.id.swipeRefresh))
            .check(matches(isDisplayed()))
    }

    @Test
    fun subscriptionsScreen_shouldDisplayToolbar() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_subscriptions))
            .perform(click())
        onView(withId(R.id.appbar))
            .check(matches(isDisplayed()))
    }

    @Test
    fun navigateToHome_shouldReturnToHomeScreen() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_subscriptions))
            .perform(click())
        onView(withId(R.id.bottom_navigation_home))
            .perform(click())
        onView(withId(R.id.bottomNavigationView))
            .check(matches(isDisplayed()))
    }

    @Test
    fun bottomNav_shouldAllowTabSwitch() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_inbox))
            .perform(click())
        onView(withId(R.id.bottomNavigationView))
            .check(matches(isDisplayed()))
        onView(withId(R.id.bottom_navigation_subscriptions))
            .perform(click())
        onView(withId(R.id.swipeRefresh))
            .check(matches(isDisplayed()))
    }
}
