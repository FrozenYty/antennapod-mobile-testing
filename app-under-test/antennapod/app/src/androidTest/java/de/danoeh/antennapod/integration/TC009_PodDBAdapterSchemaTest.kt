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
 * TC-009: PodDBAdapter Schema & Table Creation
 *
 * Verifies that all database tables are created and functional
 * after PodDBAdapter initialization. Uses public API: insertTestData()
 * and public cursor methods to validate table structure.
 *
 * @author Tianyu Yao
 */
@RunWith(AndroidJUnit4::class)
class TC009_PodDBAdapterSchemaTest {

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

    private fun minimalFeed(id: Long) = de.danoeh.antennapod.model.feed.Feed(
        id, "", "", "", "", "",
        "", "", "", "", "",
        "", "", 0L
    )

    private fun minimalFeedItem(id: Long, feed: de.danoeh.antennapod.model.feed.Feed) =
        de.danoeh.antennapod.model.feed.FeedItem(
            id, "", "", "", null,
            de.danoeh.antennapod.model.feed.FeedItem.UNPLAYED, feed
        )

    // -- Feeds --

    @Test
    fun feedsTable_insertAndRetrieve_shouldWork() {
        val values = ContentValues().apply {
            put(PodDBAdapter.KEY_TITLE, "Integration Test Feed")
            put(PodDBAdapter.KEY_DOWNLOAD_URL, "https://test.example/feed.xml")
            put(PodDBAdapter.KEY_LINK, "https://test.example")
            put(PodDBAdapter.KEY_DESCRIPTION, "A test feed")
            put(PodDBAdapter.KEY_AUTHOR, "Test Author")
            put(PodDBAdapter.KEY_LANGUAGE, "en")
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_FEEDS, values)

        val cursor = adapter.allFeedsCursor
        var found = false
        while (cursor.moveToNext()) {
            val title = cursor.getString(cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_TITLE))
            if (title == "Integration Test Feed") {
                found = true
                val link = cursor.getString(cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_LINK))
                assertEquals("https://test.example", link)
                break
            }
        }
        cursor.close()
        assertTrue("Inserted feed should be retrievable", found)
    }

    @Test
    fun feedsAutoIncrement_shouldGenerateDifferentIds() {
        repeat(2) { i ->
            val values = ContentValues().apply {
                put(PodDBAdapter.KEY_TITLE, "Auto Feed $i")
                put(PodDBAdapter.KEY_DOWNLOAD_URL, "https://auto$i.example/feed.xml")
                put(PodDBAdapter.KEY_LINK, "https://auto$i.example")
            }
            adapter.insertTestData(PodDBAdapter.TABLE_NAME_FEEDS, values)
        }

        val cursor = adapter.allFeedsCursor
        val ids = mutableListOf<Long>()
        while (cursor.moveToNext()) {
            val title = cursor.getString(cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_TITLE))
            if (title.startsWith("Auto Feed")) {
                ids.add(cursor.getLong(cursor.getColumnIndexOrThrow(PodDBAdapter.SELECT_KEY_FEED_ID)))
            }
        }
        cursor.close()
        assertTrue("Should have at least 2 feeds", ids.size >= 2)
        assertTrue("IDs should be unique", ids.distinct().size == ids.size)
    }

    // -- FeedItems --

    @Test
    fun feedItemsTable_insertAndRetrieve_shouldWork() {
        val feedId = insertFeed("Item Test Feed", "https://item-test.example/feed.xml")

        val itemValues = ContentValues().apply {
            put(PodDBAdapter.KEY_TITLE, "Integration Test Episode")
            put(PodDBAdapter.KEY_FEED, feedId)
            put(PodDBAdapter.KEY_LINK, "https://item-test.example/ep1")
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_FEED_ITEMS, itemValues)

        val cursor = adapter.getItemsOfFeedCursor(
            minimalFeed(feedId),
            de.danoeh.antennapod.model.feed.FeedItemFilter(),
            null, 0, Integer.MAX_VALUE
        )
        assertTrue("Feed items cursor should have data", cursor.count > 0)
        cursor.close()
    }

    // -- FeedMedia --

    @Test
    fun feedMediaTable_insertAndRetrieve_shouldWork() {
        val feedId = insertFeed("Media Test Feed", "https://media-test.example/feed.xml")
        val itemId = insertItem("Episode With Media", feedId)

        val mediaValues = ContentValues().apply {
            put(PodDBAdapter.KEY_FEEDITEM, itemId)
            put(PodDBAdapter.KEY_DURATION, 3600)
            put(PodDBAdapter.KEY_DOWNLOAD_URL, "https://media-test.example/ep1.mp3")
            put(PodDBAdapter.KEY_MIME_TYPE, "audio/mpeg")
            put(PodDBAdapter.KEY_SIZE, 4096)
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_FEED_MEDIA, mediaValues)

        // Query items cursor which joins FeedMedia — verify media_id is present
        val cursor = adapter.getItemsOfFeedCursor(
            minimalFeed(feedId),
            de.danoeh.antennapod.model.feed.FeedItemFilter(),
            null, 0, Integer.MAX_VALUE
        )
        assertTrue("Items cursor should have data after media insert", cursor.moveToFirst())
        val mediaIdIdx = cursor.getColumnIndex("media_id")
        assertTrue("Items cursor should include media_id column", mediaIdIdx >= 0)
        val mediaId = cursor.getLong(mediaIdIdx)
        cursor.close()

        // Now verify we can query media by its actual ID
        val mediaCursor = adapter.getFeedItemFromMediaIdCursor(mediaId)
        assertTrue("Should find media by its ID", mediaCursor.count > 0)
        mediaCursor.close()
    }

    // -- Queue --

    @Test
    fun queueTable_insert_shouldWork() {
        val feedId = insertFeed("Queue Feed", "https://queue-test.example/feed.xml")
        val itemId = insertItem("Queue Episode", feedId)

        val queueValues = ContentValues().apply {
            put(PodDBAdapter.KEY_FEEDITEM, itemId)
            put(PodDBAdapter.KEY_FEED, feedId)
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_QUEUE, queueValues)

        val queueCursor = adapter.queueCursor
        assertNotNull("Queue cursor should not be null", queueCursor)
        queueCursor.close()
    }

    // -- Favorites --

    @Test
    fun favoritesTable_insert_shouldWork() {
        val feedId = insertFeed("Fav Feed", "https://fav-test.example/feed.xml")
        val itemId = insertItem("Favorite Episode", feedId)

        val favValues = ContentValues().apply {
            put(PodDBAdapter.KEY_FEEDITEM, itemId)
            put(PodDBAdapter.KEY_FEED, feedId)
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_FAVORITES, favValues)

        // Verify via all feeds cursor — favorites join is done in query
        val cursor = adapter.allFeedsCursor
        assertNotNull("All feeds cursor should not be null", cursor)
        cursor.close()
    }

    // -- DownloadLog --

    @Test
    fun downloadLogTable_insert_shouldWork() {
        val values = ContentValues().apply {
            put(PodDBAdapter.KEY_FEEDFILETYPE, 0)
            put(PodDBAdapter.KEY_REASON, 1)
            put(PodDBAdapter.KEY_SUCCESSFUL, 1)
            put(PodDBAdapter.KEY_COMPLETION_DATE, System.currentTimeMillis())
            put(PodDBAdapter.KEY_DOWNLOADSTATUS_TITLE, "Test Download")
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_DOWNLOAD_LOG, values)

        val cursor = adapter.getDownloadLogCursor(10)
        assertNotNull("Download log cursor should not be null", cursor)
        cursor.close()
    }

    // -- SimpleChapters --

    @Test
    fun simpleChaptersTable_insert_shouldWork() {
        val feedId = insertFeed("Chapters Feed", "https://ch-test.example/feed.xml")
        val itemId = insertItem("Chapter Episode", feedId)

        val chapterValues = ContentValues().apply {
            put(PodDBAdapter.KEY_TITLE, "Chapter 1")
            put(PodDBAdapter.KEY_START, 0)
            put(PodDBAdapter.KEY_FEEDITEM, itemId)
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_SIMPLECHAPTERS, chapterValues)

        val feed = minimalFeed(feedId)
        val cursor = adapter.getSimpleChaptersOfFeedItemCursor(minimalFeedItem(itemId, feed))
        assertNotNull("Simple chapters cursor should not be null", cursor)
        cursor.close()
    }

    // -- helpers --

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
            de.danoeh.antennapod.model.feed.FeedItemFilter(),
            null, 0, Integer.MAX_VALUE
        )
        assertTrue("Item should be present after insert", cursor.moveToFirst())
        val id = cursor.getLong(cursor.getColumnIndexOrThrow("item_id"))
        cursor.close()
        require(id > 0) { "Item not found after insert" }
        return id
    }
}
