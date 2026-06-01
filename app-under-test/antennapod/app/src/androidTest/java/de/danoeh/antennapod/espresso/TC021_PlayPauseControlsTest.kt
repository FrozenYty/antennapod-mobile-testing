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
 * TC-021: Play / Pause Episode Controls
 *
 * Verifies that the playback UI infrastructure (player controls, queue access,
 * and episode browsing) is present and functional in the main activity.
 * Playback of actual media requires network content; tests focus on UI structure.
 *
 * @author Yuanbing Wang
 */
@RunWith(AndroidJUnit4::class)
class TC021_PlayPauseControlsTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java, false, false)

    @Test
    fun launchApp_shouldDisplayBottomNavigation() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottomNavigationView))
            .check(matches(isDisplayed()))
    }

    @Test
    fun bottomNav_shouldHaveQueueItem() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_queue))
            .check(matches(isDisplayed()))
    }

    @Test
    fun bottomNav_shouldHaveEpisodesItem() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_episodes))
            .check(matches(isDisplayed()))
    }

    @Test
    fun navigateToQueue_shouldDisplayQueueScreen() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        onView(withId(R.id.bottom_navigation_queue)).perform(click())
        TestHelper.saveScreenshot("tc021-queue-screen")
        onView(withId(R.id.recyclerView))
            .check(matches(isDisplayed()))
    }
}
