package de.danoeh.antennapod.uiautomator

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * TC-033: Runtime Permission Handling
 *
 * Uses UIAutomator to verify that AntennaPod exposes the runtime permission
 * surface expected by Android and that the package-specific system settings
 * screen can be opened for permission management.
 *
 * Adaptation: Android 12 test devices do not show the Android 13 notification
 * runtime dialog. The test therefore validates the manifest declaration and
 * the cross-app system settings entry point where users manage permissions.
 *
 * @author Member Four
 */
@RunWith(AndroidJUnit4::class)
class TC033_RuntimePermissionHandlingTest {

    private lateinit var context: Context
    private lateinit var device: UiDevice

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    fun manifest_shouldDeclareNotificationRuntimePermission() {
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        )

        assertTrue(
            "App should declare POST_NOTIFICATIONS for runtime permission handling",
            packageInfo.requestedPermissions.orEmpty().contains(Manifest.permission.POST_NOTIFICATIONS)
        )
    }

    @Test
    fun systemAppInfo_shouldOpenForAntennaPodPackage() {
        openAppDetailsSettings()

        val settingsRoot = device.wait(
            Until.findObject(By.pkg("com.android.settings")),
            5000
        )
        assertNotNull("System Settings should open for app details", settingsRoot)
    }

    @Test
    fun systemPermissionManagement_shouldRemainInSettingsApp() {
        openAppDetailsSettings()

        val settingsRoot = device.wait(
            Until.findObject(By.pkg("com.android.settings")),
            5000
        )
        assertNotNull("Permission management should be reachable through Settings", settingsRoot)
        assertTrue(
            "Current package should be Android Settings",
            device.currentPackageName.contains("settings")
        )
    }

    private fun openAppDetailsSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
