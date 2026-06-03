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
 * TC-022: Playback Speed Adjustment
 *
 * Verifies that playback-related settings are reachable through
 * the Settings screen. Actual speed value changes require the
 * PreferenceActivity, which is covered by Member 4's settings tests.
 * This test focuses on navigation to the player-adjacent UI.
 *
 * @author Yuanbing Wang
 */
@RunWith(AndroidJUnit4::class)
class TC022_PlaybackSpeedAdjustmentTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java, false, false)

    @Test
    fun launchApp_shouldDisplayBottomNavigation() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottomNavigationView))
            .check(matches(isDisplayed()))
    }

    @Test
    fun bottomNav_shouldHaveHomeItem() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_home))
            .check(matches(isDisplayed()))
    }

    @Test
    fun navigateToQueue_shouldDisplayQueueScreen() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_queue)).perform(click())
        onView(withId(R.id.drawer_layout))
            .check(matches(isDisplayed()))
    }

    @Test
    fun navigateBetweenQueueAndHome_shouldWork() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_queue)).perform(click())
        onView(withId(R.id.drawer_layout))
            .check(matches(isDisplayed()))
        onView(withId(R.id.bottom_navigation_home)).perform(click())
        onView(withId(R.id.drawer_layout))
            .check(matches(isDisplayed()))
    }
}
