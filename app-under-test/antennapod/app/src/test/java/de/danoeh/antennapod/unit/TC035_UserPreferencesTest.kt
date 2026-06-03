package de.danoeh.antennapod.unit

import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import de.danoeh.antennapod.model.download.ProxyConfig
import de.danoeh.antennapod.storage.preferences.UserPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.net.Proxy

/**
 * TC-035: User Preferences Read / Write Logic
 *
 * Validates key UserPreferences setters and getters used by Settings & System
 * screens. Each test starts with clean default SharedPreferences.
 *
 * @author Xintao Wang
 */
@RunWith(RobolectricTestRunner::class)
class TC035_UserPreferencesTest {

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().commit()
        UserPreferences.init(context)
    }

    @Test
    fun themePreference_setLightDarkSystem_shouldRoundTrip() {
        UserPreferences.setTheme(UserPreferences.ThemePreference.LIGHT)
        assertEquals(UserPreferences.ThemePreference.LIGHT, UserPreferences.getTheme())

        UserPreferences.setTheme(UserPreferences.ThemePreference.DARK)
        assertEquals(UserPreferences.ThemePreference.DARK, UserPreferences.getTheme())

        UserPreferences.setTheme(UserPreferences.ThemePreference.SYSTEM)
        assertEquals(UserPreferences.ThemePreference.SYSTEM, UserPreferences.getTheme())
    }

    @Test
    fun playbackPreferences_setSpeedAndSkipSilence_shouldPersist() {
        UserPreferences.setPlaybackSpeed(1.75f)
        UserPreferences.setSkipSilence(true)

        assertEquals(1.75f, UserPreferences.getPlaybackSpeed(), 0.001f)
        assertTrue(UserPreferences.isSkipSilence())

        UserPreferences.setSkipSilence(false)
        assertFalse(UserPreferences.isSkipSilence())
    }

    @Test
    fun feedRefreshInterval_setZero_shouldDisableAutoUpdate() {
        UserPreferences.setUpdateInterval(0L)

        assertEquals(0L, UserPreferences.getUpdateInterval())
        assertTrue(UserPreferences.isAutoUpdateDisabled())
    }

    @Test
    fun mobileDataPreferences_toggleFeedRefreshAndImages_shouldPersist() {
        UserPreferences.setAllowMobileFeedRefresh(true)
        UserPreferences.setAllowMobileImages(false)

        assertTrue(UserPreferences.isAllowMobileFeedRefresh())
        assertFalse(UserPreferences.isAllowMobileImages())

        UserPreferences.setAllowMobileFeedRefresh(false)
        UserPreferences.setAllowMobileImages(true)

        assertFalse(UserPreferences.isAllowMobileFeedRefresh())
        assertTrue(UserPreferences.isAllowMobileImages())
    }

    @Test
    fun notificationButtons_setCustomButtons_shouldPersist() {
        val buttons = listOf(
            UserPreferences.NOTIFICATION_BUTTON_SKIP,
            UserPreferences.NOTIFICATION_BUTTON_NEXT_CHAPTER,
            UserPreferences.NOTIFICATION_BUTTON_SLEEP_TIMER
        )

        UserPreferences.setFullNotificationButtons(buttons)

        assertEquals(buttons, UserPreferences.getFullNotificationButtons())
        assertTrue(UserPreferences.showSkipOnFullNotification())
        assertTrue(UserPreferences.showNextChapterOnFullNotification())
        assertTrue(UserPreferences.showSleepTimerOnFullNotification())
        assertFalse(UserPreferences.showPlaybackSpeedOnFullNotification())
    }

    @Test
    fun defaultPage_setQueueFragment_shouldPersist() {
        UserPreferences.setDefaultPage("QueueFragment")

        assertEquals("QueueFragment", UserPreferences.getDefaultPage())
    }

    @Test
    fun bottomNavigation_setDisabledAndEnabled_shouldPersist() {
        UserPreferences.setBottomNavigationEnabled(false)
        assertFalse(UserPreferences.isBottomNavigationEnabled())

        UserPreferences.setBottomNavigationEnabled(true)
        assertTrue(UserPreferences.isBottomNavigationEnabled())
    }

    @Test
    fun proxyConfig_setHttpProxy_shouldRoundTrip() {
        val config = ProxyConfig(
            Proxy.Type.HTTP,
            "proxy.example.com",
            8080,
            "member-four",
            "secret"
        )

        UserPreferences.setProxyConfig(config)
        val stored = UserPreferences.getProxyConfig()

        assertEquals(Proxy.Type.HTTP, stored.type)
        assertEquals("proxy.example.com", stored.host)
        assertEquals(8080, stored.port)
        assertEquals("member-four", stored.username)
        assertEquals("secret", stored.password)
    }
}
