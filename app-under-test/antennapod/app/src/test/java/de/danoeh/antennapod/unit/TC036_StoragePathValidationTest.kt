package de.danoeh.antennapod.unit

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import de.danoeh.antennapod.storage.preferences.UserPreferences
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.nio.file.Files

/**
 * TC-036: Storage Path Validation & Sanitization
 *
 * Validates data folder selection logic, including default fallback,
 * writable custom folders, type-specific subfolder creation, and invalid
 * custom path recovery.
 *
 * @author Xintao Wang
 */
@RunWith(RobolectricTestRunner::class)
class TC036_StoragePathValidationTest {

    private lateinit var context: Context
    private val temporaryRoots = mutableListOf<File>()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().commit()
        UserPreferences.init(context)
    }

    @After
    fun tearDown() {
        temporaryRoots.forEach { it.deleteRecursively() }
        temporaryRoots.clear()
    }

    @Test
    fun getDataFolder_withoutCustomPath_shouldReturnWritableDefaultFolder() {
        val folder = UserPreferences.getDataFolder("episodes")

        assertNotNull("Data folder should not be null", folder)
        assertTrue("Default data folder should exist", folder.exists())
        assertTrue("Default data folder should be writable", folder.canWrite())
        assertEquals("episodes", folder.name)
    }

    @Test
    fun getDataFolder_withWritableCustomRoot_shouldCreateTypedSubfolder() {
        val root = createTemporaryRoot("custom-root")

        UserPreferences.setDataFolder(root.absolutePath)
        val folder = UserPreferences.getDataFolder("media")

        assertEquals(File(root, "media").canonicalFile, folder.canonicalFile)
        assertTrue("Typed custom folder should be created", folder.exists())
        assertTrue("Typed custom folder should be writable", folder.canWrite())
    }

    @Test
    fun getDataFolder_withNullType_shouldReturnCustomRoot() {
        val root = createTemporaryRoot("custom-null-type")

        UserPreferences.setDataFolder(root.absolutePath)
        val folder = UserPreferences.getDataFolder(null)

        assertEquals(root.canonicalFile, folder.canonicalFile)
    }

    @Test
    fun getDataFolder_withMissingCustomBase_shouldFallbackToDefault() {
        val missingRoot = File(createTemporaryRoot("missing-parent"), "missing-child")

        UserPreferences.setDataFolder(missingRoot.absolutePath)
        val folder = UserPreferences.getDataFolder("fallback")

        assertTrue("Fallback folder should exist", folder.exists())
        assertFalse(
            "Fallback should not be created below the missing custom root",
            folder.absolutePath.startsWith(missingRoot.absolutePath)
        )
    }

    @Test
    fun setDataFolder_secondWritableRoot_shouldReplacePreviousRoot() {
        val firstRoot = createTemporaryRoot("first-root")
        val secondRoot = createTemporaryRoot("second-root")

        UserPreferences.setDataFolder(firstRoot.absolutePath)
        assertEquals(File(firstRoot, "downloads").canonicalFile,
            UserPreferences.getDataFolder("downloads").canonicalFile)

        UserPreferences.setDataFolder(secondRoot.absolutePath)
        assertEquals(File(secondRoot, "downloads").canonicalFile,
            UserPreferences.getDataFolder("downloads").canonicalFile)
    }

    private fun createTemporaryRoot(prefix: String): File {
        return Files.createTempDirectory(prefix).toFile().also { temporaryRoots.add(it) }
    }
}
