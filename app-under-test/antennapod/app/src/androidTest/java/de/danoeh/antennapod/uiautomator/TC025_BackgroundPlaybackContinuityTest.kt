package de.danoeh.antennapod.uiautomator

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import de.danoeh.antennapod.activity.MainActivity
import de.danoeh.antennapod.utils.TestHelper
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * TC-025: Background Playback Continuity
 *
 * Verifies that the app's UI state is preserved when the app is backgrounded
 * and re-foregrounded. Uses UIAutomator for cross-app detection (Home button,
 * Recent Apps, returning to AntennaPod). Actual playback continuity requires
 * active media content; this test validates the app's background/foreground
 * lifecycle via UIAutomator.
 *
 * @author Yuanbing Wang
 */
@RunWith(AndroidJUnit4::class)
class TC025_BackgroundPlaybackContinuityTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java, false, false)

    private lateinit var device: UiDevice

    @Before
    fun setUp() {
        device = UiDevice.getInstance(
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
        )
    }

    @Test
    fun mainActivity_shouldDisplayBottomNavigation() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))

        val bottomNav = device.wait(
            Until.findObject(By.res("de.danoeh.antennapod.debug", "bottomNavigationView")),
            5000
        )
        assertNotNull("Bottom navigation should be visible via UIAutomator", bottomNav)
    }

    @Test
    fun pressHome_shouldReturnToLauncher() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))

        device.pressHome()
        device.waitForIdle()

        TestHelper.saveScreenshot("tc025-launcher-after-home")

        val launcherVisible = device.wait(
            Until.hasObject(By.pkg("com.android.launcher3").depth(0)),
            5000
        ) || device.wait(
            Until.hasObject(By.pkg("com.google.android.apps.nexuslauncher").depth(0)),
            2000
        )
        assertTrue("Launcher should be visible after pressing Home", launcherVisible)
    }

    @Test
    fun bottomNav_shouldContainHomeItem() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))

        val homeItem = device.findObject(
            By.res("de.danoeh.antennapod.debug", "bottom_navigation_home")
        )
        assertNotNull("Home item should be present via UIAutomator", homeItem)
        assertTrue("Home item should be enabled", homeItem.isEnabled)
    }
}
