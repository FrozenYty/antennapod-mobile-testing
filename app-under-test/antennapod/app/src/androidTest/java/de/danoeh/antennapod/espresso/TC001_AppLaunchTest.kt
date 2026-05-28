package de.danoeh.antennapod.espresso

import android.content.Intent
import androidx.test.espresso.Espresso.onView
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
 * TC-001: App Launch & Main Screen
 *
 * Verifies that the app launches successfully and the main screen
 * displays the expected UI elements (bottom navigation, toolbar area, content area).
 *
 * @author Tianyu Yao
 */
@RunWith(AndroidJUnit4::class)
class TC001_AppLaunchTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java, false, false)

    @Test
    fun launchApp_shouldDisplayBottomNavigation() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        TestHelper.saveScreenshot("tc001-launch-home")
        onView(withId(R.id.bottomNavigationView))
            .check(matches(isDisplayed()))
    }

    @Test
    fun launchApp_shouldDisplayAppBar() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.appbar))
            .check(matches(isDisplayed()))
    }

    @Test
    fun launchApp_shouldDisplayDrawerLayout() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.drawer_layout))
            .check(matches(isDisplayed()))
    }

    @Test
    fun bottomNav_shouldHaveHomeItem() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_home))
            .check(matches(isDisplayed()))
    }

    @Test
    fun bottomNav_shouldHaveSubscriptionsItem() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_subscriptions))
            .check(matches(isDisplayed()))
    }

    @Test
    fun bottomNav_shouldHaveMoreItem() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_more))
            .check(matches(isDisplayed()))
    }
}
