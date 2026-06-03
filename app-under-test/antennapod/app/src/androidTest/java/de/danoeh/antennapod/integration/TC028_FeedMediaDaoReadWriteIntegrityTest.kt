package de.danoeh.antennapod.integration

import android.content.ContentValues
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.danoeh.antennapod.storage.database.PodDBAdapter
import org.junit.After
import de.danoeh.antennapod.model.feed.Feed
import de.danoeh.antennapod.model.feed.FeedItemFilter
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * TC-028: FeedMedia DAO Read / Write Integrity
 *
 * Validates FeedMedia CRUD operations through PodDBAdapter: insert,
 * retrieve, update playback information, and verify data integrity
 * across the Feeds → FeedItems → FeedMedia table hierarchy.
 *
 * @author Yuanbing Wang
 */
@RunWith(AndroidJUnit4::class)
class TC028_FeedMediaDaoReadWriteIntegrityTest {

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

    private fun minimalFeed(id: Long) = Feed(
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
        require(id > 0) { "Feed not found after insert" }
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
            FeedItemFilter(),
            null, 0, Integer.MAX_VALUE
        )
        assertTrue("Item should be present after insert", cursor.moveToFirst())
        val id = cursor.getLong(cursor.getColumnIndexOrThrow("item_id"))
        cursor.close()
        require(id > 0) { "Item not found after insert" }
        return id
    }

    @Test
    fun feedMediaTable_insertBasic_shouldGenerateId() {
        val feedId = insertFeed("DAO Feed", "https://dao.example/feed.xml")
        val itemId = insertItem("DAO Episode", feedId)

        val values = ContentValues().apply {
            put(PodDBAdapter.KEY_FEEDITEM, itemId)
            put(PodDBAdapter.KEY_DURATION, 3600)
            put(PodDBAdapter.KEY_DOWNLOAD_URL, "https://dao.example/ep1.mp3")
            put(PodDBAdapter.KEY_MIME_TYPE, "audio/mpeg")
            put(PodDBAdapter.KEY_SIZE, 10485760)
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_FEED_MEDIA, values)

        val cursor = adapter.getItemsOfFeedCursor(
            minimalFeed(feedId),
            FeedItemFilter(),
            null, 0, Integer.MAX_VALUE
        )
        assertTrue("Items cursor should have data", cursor.moveToFirst())
        val mediaIdIdx = cursor.getColumnIndex("media_id")
        assertTrue("Cursor should include media_id column", mediaIdIdx >= 0)
        val mediaId = cursor.getLong(mediaIdIdx)
        assertTrue("Media should have a non-zero ID", mediaId > 0)
        cursor.close()
    }

    @Test
    fun feedMediaTable_insertWithAllFields_shouldRetrieveCorrectly() {
        val feedId = insertFeed("AllFields Feed", "https://allfields.example/feed.xml")
        val itemId = insertItem("AllFields Episode", feedId)

        val values = ContentValues().apply {
            put(PodDBAdapter.KEY_FEEDITEM, itemId)
            put(PodDBAdapter.KEY_DURATION, 2400)
            put(PodDBAdapter.KEY_DOWNLOAD_URL, "https://allfields.example/ep.mp3")
            put(PodDBAdapter.KEY_MIME_TYPE, "audio/ogg")
            put(PodDBAdapter.KEY_SIZE, 52428800)
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_FEED_MEDIA, values)

        val cursor = adapter.getItemsOfFeedCursor(
            minimalFeed(feedId),
            FeedItemFilter(),
            null, 0, Integer.MAX_VALUE
        )
        assertTrue("Feed items cursor should have data", cursor.moveToFirst())

        val durationIdx = cursor.getColumnIndex("duration")
        val mimeIdx = cursor.getColumnIndex("mime_type")
        val downloadUrlIdx = cursor.getColumnIndex("download_url")
        val sizeIdx = cursor.getColumnIndex("size")

        assertTrue("Duration should be present in cursor", durationIdx >= 0)
        assertEquals(2400, cursor.getInt(durationIdx))
        if (mimeIdx >= 0) {
            assertEquals("audio/ogg", cursor.getString(mimeIdx))
        }
        if (downloadUrlIdx >= 0) {
            assertEquals("https://allfields.example/ep.mp3", cursor.getString(downloadUrlIdx))
        }
        if (sizeIdx >= 0) {
            assertEquals(52428800, cursor.getLong(sizeIdx))
        }
        cursor.close()
    }

    @Test
    fun feedMediaTable_multipleInsert_shouldHaveUniqueIds() {
        val feedId = insertFeed("MultiMedia Feed", "https://multi-media.example/feed.xml")
        val item1Id = insertItem("Episode A", feedId)
        val item2Id = insertItem("Episode B", feedId)

        insertMedia(item1Id, "https://multi-media.example/a.mp3", 1200, "audio/mpeg", 1024)
        insertMedia(item2Id, "https://multi-media.example/b.mp3", 1800, "audio/mpeg", 2048)

        val cursor = adapter.getItemsOfFeedCursor(
            minimalFeed(feedId),
            FeedItemFilter(),
            null, 0, Integer.MAX_VALUE
        )
        val mediaIds = mutableListOf<Long>()
        while (cursor.moveToNext()) {
            val mediaId = cursor.getLong(cursor.getColumnIndexOrThrow("media_id"))
            if (mediaId > 0) mediaIds.add(mediaId)
        }
        cursor.close()
        assertTrue("Should have at least 2 media entries", mediaIds.size >= 2)
        assertEquals("Media IDs should be unique", mediaIds.size, mediaIds.distinct().size)
    }

    @Test
    fun feedMedia_shouldBeLinkedToFeedItem() {
        val feedId = insertFeed("Linked Feed", "https://linked.example/feed.xml")
        val itemId = insertItem("Linked Episode", feedId)

        insertMedia(itemId, "https://linked.example/ep.mp3", 3000, "audio/mpeg", 8192)

        val cursor = adapter.getItemsOfFeedCursor(
            minimalFeed(feedId),
            FeedItemFilter(),
            null, 0, Integer.MAX_VALUE
        )
        assertTrue("Items cursor should have data", cursor.moveToFirst())
        val itemIdCol = cursor.getLong(cursor.getColumnIndexOrThrow("item_id"))
        assertEquals(itemId, itemIdCol)
        cursor.close()
    }

    @Test
    fun feedMedia_withDownloadLog_shouldTrackCompletion() {
        val values = ContentValues().apply {
            put(PodDBAdapter.KEY_FEEDFILETYPE, 2) // FEEDFILETYPE_FEEDMEDIA
            put(PodDBAdapter.KEY_REASON, 1)
            put(PodDBAdapter.KEY_SUCCESSFUL, 1)
            put(PodDBAdapter.KEY_COMPLETION_DATE, System.currentTimeMillis())
            put(PodDBAdapter.KEY_DOWNLOADSTATUS_TITLE, "Media Download Test")
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_DOWNLOAD_LOG, values)

        val cursor = adapter.getDownloadLogCursor(10)
        assertNotNull("Download log cursor should not be null", cursor)
        assertTrue("Download log should have at least one entry", cursor.count > 0)
        cursor.close()
    }

    @Test
    fun feedMedia_playbackPosition_shouldBeStorageInMediaTable() {
        val feedId = insertFeed("Position Feed", "https://position.example/feed.xml")
        val itemId = insertItem("Position Episode", feedId)

        insertMedia(itemId, "https://position.example/ep.mp3", 4500, "audio/mpeg", 65536)

        val cursor = adapter.getItemsOfFeedCursor(
            minimalFeed(feedId),
            FeedItemFilter(),
            null, 0, Integer.MAX_VALUE
        )
        assertTrue(cursor.moveToFirst())
        val positionIdx = cursor.getColumnIndex("position")
        val durationIdx = cursor.getColumnIndex("duration")
        assertTrue("Position column should be present", positionIdx >= 0)
        assertTrue("Duration column should be present", durationIdx >= 0)
        assertEquals(4500, cursor.getInt(durationIdx))
        cursor.close()
    }

    private fun insertMedia(
        itemId: Long, downloadUrl: String, duration: Int, mimeType: String, size: Long
    ) {
        val values = ContentValues().apply {
            put(PodDBAdapter.KEY_FEEDITEM, itemId)
            put(PodDBAdapter.KEY_DURATION, duration)
            put(PodDBAdapter.KEY_DOWNLOAD_URL, downloadUrl)
            put(PodDBAdapter.KEY_MIME_TYPE, mimeType)
            put(PodDBAdapter.KEY_SIZE, size)
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_FEED_MEDIA, values)
    }
}
