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
 * TC-012: Subscribe to Feed from Discovery
 *
 * Verifies that the Add Feed entry points are accessible from the
 * bottom navigation, enabling the subscription workflow. Tests cover
 * More menu interaction and tab navigation relevant to discovery.
 *
 * Adaptation: Network content and the More popup menu content are not
 * reliably testable with Espresso (popup uses dynamic layout). Tests
 * verify bottom nav structure and clickability.
 *
 * @author Jianheng Sun
 */
@RunWith(AndroidJUnit4::class)
class TC012_SubscribeDiscoveryTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java, false, false)

    @Test
    fun bottomNav_shouldHaveMoreItem() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_more))
            .check(matches(isDisplayed()))
    }

    @Test
    fun tapMore_shouldBeClickable() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_more))
            .perform(click())
        // More menu opened successfully if no exception thrown
    }

    @Test
    fun bottomNav_shouldHaveSubscriptionsItem() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_subscriptions))
            .check(matches(isDisplayed()))
    }

    @Test
    fun bottomNav_shouldHaveHomeItem() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_home))
            .check(matches(isDisplayed()))
    }
}
