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
 * TC-023: Download Episode for Offline Playback
 *
 * Verifies that the UI elements supporting episode downloads
 * are present and accessible. Actual downloads require network
 * content and subscribed feeds; the test focuses on UI structure
 * and navigation to the downloads-related screens.
 *
 * @author Yuanbing Wang
 */
@RunWith(AndroidJUnit4::class)
class TC023_DownloadEpisodeForOfflinePlaybackTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java, false, false)

    @Test
    fun launchApp_shouldDisplayBottomNavigation() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottomNavigationView))
            .check(matches(isDisplayed()))
    }

    @Test
    fun bottomNav_shouldHaveMoreItem() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_more))
            .check(matches(isDisplayed()))
    }

    @Test
    fun tapMore_shouldBeClickable() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_more)).perform(click())
        TestHelper.saveScreenshot("tc023-more-menu")
    }

    @Test
    fun bottomNav_shouldHaveInboxItem() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_inbox))
            .check(matches(isDisplayed()))
    }
}
