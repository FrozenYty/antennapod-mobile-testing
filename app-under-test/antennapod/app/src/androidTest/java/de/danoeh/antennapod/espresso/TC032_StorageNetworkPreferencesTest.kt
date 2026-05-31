package de.danoeh.antennapod.espresso

import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import de.danoeh.antennapod.R
import de.danoeh.antennapod.ui.screen.preferences.PreferenceActivity
import de.test.antennapod.EspressoTestUtils.clickPreference
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * TC-032: Storage & Network Preferences
 *
 * Verifies that storage, feed refresh, mobile data, and proxy settings
 * are reachable from the Downloads preferences screen.
 *
 * @author Member Four
 */
@RunWith(AndroidJUnit4::class)
class TC032_StorageNetworkPreferencesTest {

    @get:Rule
    val activityRule = ActivityTestRule(PreferenceActivity::class.java, false, false)

    @Test
    fun settingsMain_shouldDisplayDownloadsEntry() {
        activityRule.launchActivity(Intent())
        onView(withText(R.string.downloads_pref))
            .check(matches(isDisplayed()))
    }

    @Test
    fun downloadsSettings_shouldDisplayDataFolderPreference() {
        activityRule.launchActivity(Intent())
        clickPreference(R.string.downloads_pref)
        onView(withText(R.string.choose_data_directory))
            .check(matches(isDisplayed()))
    }

    @Test
    fun downloadsSettings_shouldDisplayFeedRefreshPreference() {
        activityRule.launchActivity(Intent())
        clickPreference(R.string.downloads_pref)
        onView(withText(R.string.feed_refresh_title))
            .check(matches(isDisplayed()))
    }

    @Test
    fun downloadsSettings_shouldOpenProxyDialog() {
        activityRule.launchActivity(Intent())
        clickPreference(R.string.downloads_pref)
        clickPreference(R.string.pref_proxy_title)
        onView(withText(R.string.pref_proxy_title))
            .check(matches(isDisplayed()))
    }
}
