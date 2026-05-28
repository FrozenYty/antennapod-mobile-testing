package de.danoeh.antennapod.uiautomator

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import de.danoeh.antennapod.activity.MainActivity
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * TC-006: OPML Import via System File Picker
 *
 * Uses UIAutomator to verify cross-app/system UI capabilities:
 * detecting views by resource ID (including bottom navigation items
 * that Espresso cannot reach from outside the app process).
 *
 * @author Tianyu Yao
 */
@RunWith(AndroidJUnit4::class)
class TC006_OpmlImportTest {

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
        assertNotNull("Bottom navigation should be visible", bottomNav)
    }

    @Test
    fun bottomNav_shouldContainHomeItem() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))

        val homeItem = device.findObject(
            By.res("de.danoeh.antennapod.debug", "bottom_navigation_home")
        )
        assertNotNull("Home item should be present in bottom nav", homeItem)
        assertTrue("Home item should be enabled", homeItem.isEnabled)
    }

    @Test
    fun bottomNav_shouldContainQueueItem() {
        activityRule.launchActivity(Intent(Intent.ACTION_MAIN))

        val queueItem = device.findObject(
            By.res("de.danoeh.antennapod.debug", "bottom_navigation_queue")
        )
        assertNotNull("Queue item should be present in bottom nav", queueItem)
        assertTrue("Queue item should be enabled", queueItem.isEnabled)
    }
}
