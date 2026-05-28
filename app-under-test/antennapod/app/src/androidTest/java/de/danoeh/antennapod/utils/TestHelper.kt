package de.danoeh.antennapod.utils

import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.io.FileOutputStream

/**
 * Shared test utilities for all test methods.
 *
 * @author Tianyu Yao
 */
object TestHelper {

    private const val TAG = "TestHelper"
    private const val SCREENSHOTS_DIR = "screenshots"

    /**
     * Saves a screenshot of the current device screen to the app's external files directory.
     * Pull files after test run:
     *   adb pull /sdcard/Android/data/de.danoeh.antennapod.debug/files/screenshots/ ./screenshots/
     *
     * @param name file name without extension (e.g. "tc001-launch-home")
     */
    @JvmStatic
    fun saveScreenshot(name: String) {
        try {
            val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
            val bitmap: Bitmap = uiAutomation.takeScreenshot()
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                SCREENSHOTS_DIR
            )
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val file = File(dir, "$name.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            Log.i(TAG, "Screenshot saved: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save screenshot '$name'", e)
        }
    }
}
