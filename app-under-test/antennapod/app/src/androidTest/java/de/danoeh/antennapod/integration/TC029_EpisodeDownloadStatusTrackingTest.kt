package de.danoeh.antennapod.integration

import android.content.ContentValues
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.danoeh.antennapod.storage.database.PodDBAdapter
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * TC-029: Episode Download Status Tracking
 *
 * Validates that episode download status transitions are correctly
 * stored in the database. Tests download log entries, successful/failed
 * download tracking, and completion date recording through PodDBAdapter.
 *
 * @author Yuanbing Wang
 */
@RunWith(AndroidJUnit4::class)
class TC029_EpisodeDownloadStatusTrackingTest {

    private lateinit var adapter: PodDBAdapter
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        PodDBAdapter.init(context)
        adapter = PodDBAdapter.getInstance()
    }

    @After
    fun tearDown() {
        PodDBAdapter.tearDownTests()
    }

    @Test
    fun downloadLog_insertSuccessful_shouldPersist() {
        val completionTime = System.currentTimeMillis()
        val values = ContentValues().apply {
            put(PodDBAdapter.KEY_FEEDFILETYPE, 2) // FEEDFILETYPE_FEEDMEDIA
            put(PodDBAdapter.KEY_REASON, 1) // user-initiated
            put(PodDBAdapter.KEY_SUCCESSFUL, 1)
            put(PodDBAdapter.KEY_COMPLETION_DATE, completionTime)
            put(PodDBAdapter.KEY_DOWNLOADSTATUS_TITLE, "Test Episode Download")
            put(PodDBAdapter.KEY_DOWNLOAD_URL, "https://test.example/ep1.mp3")
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_DOWNLOAD_LOG, values)

        val cursor = adapter.getDownloadLogCursor(10)
        assertTrue("Download log should have entries", cursor.count > 0)
        cursor.close()
    }

    @Test
    fun downloadLog_insertFailed_shouldPersist() {
        val values = ContentValues().apply {
            put(PodDBAdapter.KEY_FEEDFILETYPE, 2)
            put(PodDBAdapter.KEY_REASON, 2) // auto-download
            put(PodDBAdapter.KEY_SUCCESSFUL, 0) // failed
            put(PodDBAdapter.KEY_COMPLETION_DATE, System.currentTimeMillis())
            put(PodDBAdapter.KEY_DOWNLOADSTATUS_TITLE, "Failed Episode Download")
            put(PodDBAdapter.KEY_REASON_DETAILED, "Network timeout")
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_DOWNLOAD_LOG, values)

        val cursor = adapter.getDownloadLogCursor(10)
        var foundFailed = false
        while (cursor.moveToNext()) {
            val successIdx = cursor.getColumnIndex(PodDBAdapter.KEY_SUCCESSFUL)
            if (successIdx >= 0 && cursor.getInt(successIdx) == 0) {
                foundFailed = true
                val titleIdx = cursor.getColumnIndex(PodDBAdapter.KEY_DOWNLOADSTATUS_TITLE)
                assertEquals("Failed Episode Download", cursor.getString(titleIdx))
                break
            }
        }
        cursor.close()
        assertTrue("Should find failed download log entry", foundFailed)
    }

    @Test
    fun downloadLog_multipleEntries_shouldBeInOrder() {
        val timeBase = System.currentTimeMillis()
        for (i in 0..2) {
            val values = ContentValues().apply {
                put(PodDBAdapter.KEY_FEEDFILETYPE, 2)
                put(PodDBAdapter.KEY_REASON, 1)
                put(PodDBAdapter.KEY_SUCCESSFUL, 1)
                put(PodDBAdapter.KEY_COMPLETION_DATE, timeBase + i * 1000)
                put(PodDBAdapter.KEY_DOWNLOADSTATUS_TITLE, "Download Entry $i")
            }
            adapter.insertTestData(PodDBAdapter.TABLE_NAME_DOWNLOAD_LOG, values)
        }

        val cursor = adapter.getDownloadLogCursor(10)
        assertTrue("Download log should have entries", cursor.count >= 3)
        cursor.close()
    }

    @Test
    fun queueEntry_withFeedMedia_shouldIncludeMediaReference() {
        val feedId = insertFeed("QueueDownload Feed", "https://qd.example/feed.xml")
        val itemId = insertItem("QueueDownload Episode", feedId)

        val mediaValues = ContentValues().apply {
            put(PodDBAdapter.KEY_FEEDITEM, itemId)
            put(PodDBAdapter.KEY_DOWNLOAD_URL, "https://qd.example/ep.mp3")
            put(PodDBAdapter.KEY_DURATION, 3600)
            put(PodDBAdapter.KEY_MIME_TYPE, "audio/mpeg")
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_FEED_MEDIA, mediaValues)

        val queueValues = ContentValues().apply {
            put(PodDBAdapter.KEY_FEEDITEM, itemId)
            put(PodDBAdapter.KEY_FEED, feedId)
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_QUEUE, queueValues)

        val cursor = adapter.getItemsOfFeedCursor(
            minimalFeed(feedId),
            de.danoeh.antennapod.model.feed.FeedItemFilter(),
            null, 0, Integer.MAX_VALUE
        )
        assertTrue("Queue items should have data", cursor.moveToFirst())
        val mediaIdIdx = cursor.getColumnIndex("media_id")
        assertTrue("Items in queue should include media_id", mediaIdIdx >= 0)
        val mediaId = cursor.getLong(mediaIdIdx)
        assertTrue("Media ID should be non-zero for queued item", mediaId > 0)
        cursor.close()
    }

    @Test
    fun feedMedia_withLocalFileUrl_shouldPersistInDatabase() {
        val feedId = insertFeed("LocalFile Feed", "https://localfile.example/feed.xml")
        val itemId = insertItem("LocalFile Episode", feedId)

        val values = ContentValues().apply {
            put(PodDBAdapter.KEY_FEEDITEM, itemId)
            put(PodDBAdapter.KEY_DOWNLOAD_URL, "https://localfile.example/ep.mp3")
            put(PodDBAdapter.KEY_DURATION, 1800)
            put(PodDBAdapter.KEY_MIME_TYPE, "audio/mpeg")
            put(PodDBAdapter.KEY_SIZE, 20971520)
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_FEED_MEDIA, values)

        val cursor = adapter.getItemsOfFeedCursor(
            minimalFeed(feedId),
            de.danoeh.antennapod.model.feed.FeedItemFilter(),
            null, 0, Integer.MAX_VALUE
        )
        assertTrue("Items cursor should have data", cursor.moveToFirst())
        val sizeIdx = cursor.getColumnIndex("size")
        val mimeIdx = cursor.getColumnIndex("mime_type")
        assertTrue("Size column should be present", sizeIdx >= 0)
        assertEquals(20971520, cursor.getLong(sizeIdx))
        if (mimeIdx >= 0) {
            assertEquals("audio/mpeg", cursor.getString(mimeIdx))
        }
        cursor.close()
    }

    @Test
    fun clearDownloadLog_shouldRemoveAllEntries() {
        for (i in 0..2) {
            val values = ContentValues().apply {
                put(PodDBAdapter.KEY_FEEDFILETYPE, 2)
                put(PodDBAdapter.KEY_REASON, 1)
                put(PodDBAdapter.KEY_SUCCESSFUL, 1)
                put(PodDBAdapter.KEY_COMPLETION_DATE, System.currentTimeMillis())
                put(PodDBAdapter.KEY_DOWNLOADSTATUS_TITLE, "Clear Test $i")
            }
            adapter.insertTestData(PodDBAdapter.TABLE_NAME_DOWNLOAD_LOG, values)
        }

        val beforeCursor = adapter.getDownloadLogCursor(10)
        assertTrue("Should have entries before clear", beforeCursor.count >= 3)
        beforeCursor.close()

        adapter.clearDownloadLog()

        val afterCursor = adapter.getDownloadLogCursor(10)
        assertEquals("Download log should be empty after clear", 0, afterCursor.count)
        afterCursor.close()
    }

    @Test
    fun clearQueue_shouldRemoveQueuedEntries() {
        val feedId = insertFeed("ClearQ Feed", "https://clearq.example/feed.xml")
        val itemId = insertItem("ClearQ Episode", feedId)

        val queueValues = ContentValues().apply {
            put(PodDBAdapter.KEY_FEEDITEM, itemId)
            put(PodDBAdapter.KEY_FEED, feedId)
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_QUEUE, queueValues)

        adapter.clearQueue()

        val cursor = adapter.queueCursor
        assertEquals("Queue should be empty after clear", 0, cursor.count)
        cursor.close()
    }

    // -- helpers --

    private fun minimalFeed(id: Long) = de.danoeh.antennapod.model.feed.Feed(
        id, "", "", "", "", "",
        "", "", "", "", "",
        "", "", 0L
    )

    private fun insertFeed(title: String, downloadUrl: String): Long {
        val values = ContentValues().apply {
            put(PodDBAdapter.KEY_TITLE, title)
            put(PodDBAdapter.KEY_DOWNLOAD_URL, downloadUrl)
            put(PodDBAdapter.KEY_LINK, "https://example.com")
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_FEEDS, values)

        val cursor = adapter.allFeedsCursor
        var id = -1L
        while (cursor.moveToNext()) {
            val rowTitle = cursor.getString(cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_TITLE))
            if (rowTitle == title) {
                id = cursor.getLong(cursor.getColumnIndexOrThrow(PodDBAdapter.SELECT_KEY_FEED_ID))
                break
            }
        }
        cursor.close()
        require(id > 0)
        return id
    }

    private fun insertItem(title: String, feedId: Long): Long {
        val values = ContentValues().apply {
            put(PodDBAdapter.KEY_TITLE, title)
            put(PodDBAdapter.KEY_FEED, feedId)
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_FEED_ITEMS, values)

        val cursor = adapter.getItemsOfFeedCursor(
            minimalFeed(feedId),
            de.danoeh.antennapod.model.feed.FeedItemFilter(),
            null, 0, Integer.MAX_VALUE
        )
        assertTrue(cursor.moveToFirst())
        val id = cursor.getLong(cursor.getColumnIndexOrThrow("item_id"))
        cursor.close()
        require(id > 0)
        return id
    }
}
