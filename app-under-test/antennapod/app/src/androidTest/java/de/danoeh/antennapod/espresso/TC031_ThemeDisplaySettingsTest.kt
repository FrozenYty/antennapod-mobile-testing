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
 * TC-031: Theme & Display Settings
 *
 * Verifies that the settings entry points for theme and display preferences
 * are reachable and expose the expected controls.
 *
 * @author Xintao Wang
 */
@RunWith(AndroidJUnit4::class)
class TC031_ThemeDisplaySettingsTest {

    @get:Rule
    val activityRule = ActivityTestRule(PreferenceActivity::class.java, false, false)

    @Test
    fun settingsMain_shouldDisplayUserInterfaceEntry() {
        activityRule.launchActivity(Intent())
        onView(withText(R.string.user_interface_label))
            .check(matches(isDisplayed()))
    }

    @Test
    fun userInterfaceSettings_shouldDisplayThemeControls() {
        activityRule.launchActivity(Intent())
        clickPreference(R.string.user_interface_label)
        onView(withText(R.string.pref_black_theme_title))
            .check(matches(isDisplayed()))
    }

    @Test
    fun userInterfaceSettings_shouldDisplayEpisodeCoverControl() {
        activityRule.launchActivity(Intent())
        clickPreference(R.string.user_interface_label)
        onView(withText(R.string.pref_episode_cover_title))
            .check(matches(isDisplayed()))
    }

    @Test
    fun userInterfaceSettings_shouldDisplayTintedThemeControl() {
        activityRule.launchActivity(Intent())
        clickPreference(R.string.user_interface_label)
        onView(withText(R.string.pref_tinted_theme_title))
            .check(matches(isDisplayed()))
    }
}
