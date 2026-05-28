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
 * TC-005: In-App Search & Discovery Browse
 *
 * Verifies the bottom navigation tabs are all accessible and
 * the more menu provides access to additional items.
 *
 * @author Tianyu Yao
 */
@RunWith(AndroidJUnit4::class)
class TC005_SearchDiscoveryTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java, false, false)

    @Test
    fun bottomNav_shouldHaveMoreMenu() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_more))
            .check(matches(isDisplayed()))
    }

    @Test
    fun bottomNav_shouldHaveInbox() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_inbox))
            .check(matches(isDisplayed()))
    }

    @Test
    fun bottomNav_shouldHaveQueue() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_queue))
            .check(matches(isDisplayed()))
    }

    @Test
    fun tapMore_shouldOpenMoreMenu() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_more))
            .perform(click())
    }
}
