package de.danoeh.antennapod.uiautomator

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import de.danoeh.antennapod.storage.preferences.UserPreferences
import de.danoeh.antennapod.ui.notifications.NotificationUtils
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * TC-034: Notification Channel Settings
 *
 * Verifies that AntennaPod creates the expected notification channels and
 * exposes the app notification settings screen through Android Settings.
 *
 * @author Xintao Wang
 */
@RunWith(AndroidJUnit4::class)
class TC034_NotificationChannelSettingsTest {

    private lateinit var context: Context
    private lateinit var device: UiDevice
    private lateinit var notificationManager: NotificationManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        UserPreferences.init(context)
        notificationManager = context.getSystemService(NotificationManager::class.java)
    }

    @Test
    fun createChannels_shouldRegisterPlaybackAndDownloadChannels() {
        assumeTrue("Notification channels require Android O or newer", Build.VERSION.SDK_INT >= 26)

        NotificationUtils.createChannels(context)

        assertNotNull(
            "Playback channel should be registered",
            notificationManager.getNotificationChannel(NotificationUtils.CHANNEL_ID_PLAYING)
        )
        assertNotNull(
            "Downloading channel should be registered",
            notificationManager.getNotificationChannel(NotificationUtils.CHANNEL_ID_DOWNLOADING)
        )
        assertNotNull(
            "Refresh channel should be registered",
            notificationManager.getNotificationChannel(NotificationUtils.CHANNEL_ID_REFRESHING)
        )
    }

    @Test
    fun createChannels_shouldRegisterErrorAndNewsGroups() {
        assumeTrue("Notification channel groups require Android O or newer", Build.VERSION.SDK_INT >= 26)

        NotificationUtils.createChannels(context)
        val groupIds = notificationManager.notificationChannelGroups.map { it.id }

        assertTrue(
            "Error channel group should be registered",
            groupIds.contains(NotificationUtils.GROUP_ID_ERRORS)
        )
        assertTrue(
            "News channel group should be registered",
            groupIds.contains(NotificationUtils.GROUP_ID_NEWS)
        )
    }

    @Test
    fun appNotificationSettings_shouldOpenSystemNotificationScreen() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)

        val settingsRoot = device.wait(
            Until.findObject(By.pkg("com.android.settings")),
            5000
        )
        assertNotNull("Android notification settings should open", settingsRoot)
        assertTrue(
            "Current package should be Android Settings",
            device.currentPackageName.contains("settings")
        )
    }
}
