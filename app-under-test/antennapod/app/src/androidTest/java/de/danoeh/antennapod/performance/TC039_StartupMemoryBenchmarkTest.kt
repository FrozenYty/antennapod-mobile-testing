package de.danoeh.antennapod.performance

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import de.danoeh.antennapod.activity.MainActivity
import de.danoeh.antennapod.ui.screen.preferences.PreferenceActivity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * TC-039: App Startup Time & Memory Footprint
 *
 * Measures startup and memory characteristics with manual timing because no
 * benchmark library is configured in this project.
 *
 * @author Xintao Wang
 */
@RunWith(AndroidJUnit4::class)
class TC039_StartupMemoryBenchmarkTest {

    @get:Rule
    val mainActivityRule = ActivityTestRule(MainActivity::class.java, false, false)

    @get:Rule
    val preferenceActivityRule = ActivityTestRule(PreferenceActivity::class.java, false, false)

    @Test
    fun benchmark_mainActivityLaunch_shouldBeUnderFiveSeconds() {
        val elapsedMillis = measureLaunchMillis {
            mainActivityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        }

        assertTrue(
            "MainActivity launch time ${elapsedMillis}ms exceeds 5000ms threshold",
            elapsedMillis < 5000
        )
    }

    @Test
    fun benchmark_preferencesLaunch_shouldBeUnderThreeSeconds() {
        val elapsedMillis = measureLaunchMillis {
            preferenceActivityRule.launchActivity(Intent())
        }

        assertTrue(
            "PreferenceActivity launch time ${elapsedMillis}ms exceeds 3000ms threshold",
            elapsedMillis < 3000
        )
    }

    @Test
    fun benchmark_repeatedMainActivityLaunch_shouldAverageUnderThreeSeconds() {
        val iterations = 3
        var totalMillis = 0L

        repeat(iterations) {
            totalMillis += measureLaunchMillis {
                mainActivityRule.launchActivity(Intent(Intent.ACTION_MAIN))
            }
            mainActivityRule.finishActivity()
        }

        val averageMillis = totalMillis / iterations
        assertTrue(
            "Average MainActivity launch time ${averageMillis}ms exceeds 3000ms threshold",
            averageMillis < 3000
        )
    }

    @Test
    fun memory_afterMainActivityLaunch_shouldStayBelow256Mb() {
        mainActivityRule.launchActivity(Intent(Intent.ACTION_MAIN))
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        val usedMb = usedMemoryMb()

        assertTrue(
            "Used memory ${usedMb}MB exceeds 256MB threshold after startup",
            usedMb < 256
        )
    }

    private fun measureLaunchMillis(launch: () -> Unit): Long {
        val start = System.nanoTime()
        launch()
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        return (System.nanoTime() - start) / 1_000_000
    }

    private fun usedMemoryMb(): Long {
        val runtime = Runtime.getRuntime()
        runtime.gc()
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
    }
}
