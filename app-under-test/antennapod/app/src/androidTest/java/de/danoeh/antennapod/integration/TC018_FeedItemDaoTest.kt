package de.danoeh.antennapod.integration

import android.content.ContentValues
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.danoeh.antennapod.storage.database.PodDBAdapter
import org.junit.After
import de.danoeh.antennapod.model.feed.Feed
import de.danoeh.antennapod.model.feed.FeedItemFilter
import de.danoeh.antennapod.model.feed.SortOrder
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * TC-018: Feed & FeedItem DAO Query Correctness
 *
 * Verifies Feed and FeedItem CRUD operations using PodDBAdapter.
 * Tests feed insertion with all fields, feed state transitions,
 * item queries with sort orders, and filter query behavior.
 *
 * @author Jianheng Sun
 */
@RunWith(AndroidJUnit4::class)
class TC018_FeedItemDaoTest {

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
            put(PodDBAdapter.KEY_LINK, "https://example.com/ep")
            put(PodDBAdapter.KEY_PUBDATE, System.currentTimeMillis())
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_FEED_ITEMS, values)

        val feed = Feed(
            feedId, "", "", "", "", "",
            "", "", "", "", "",
            "", "", 0L
        )
        val cursor = adapter.getItemsOfFeedCursor(
            feed,
            FeedItemFilter(),
            null, 0, Integer.MAX_VALUE
        )
        assertTrue("Should find inserted item", cursor.moveToFirst())
        val id = cursor.getLong(cursor.getColumnIndexOrThrow("item_id"))
        cursor.close()
        require(id > 0) { "Item not found after insert" }
        return id
    }

    // -- Feeds CRUD --

    @Test
    fun feedsTable_insertWithAllFields_shouldRetrieveCorrectly() {
        val values = ContentValues().apply {
            put(PodDBAdapter.KEY_TITLE, "Full Field Feed")
            put(PodDBAdapter.KEY_DOWNLOAD_URL, "https://full.example/feed.xml")
            put(PodDBAdapter.KEY_LINK, "https://full.example")
            put(PodDBAdapter.KEY_DESCRIPTION, "A complete feed description")
            put(PodDBAdapter.KEY_AUTHOR, "Test Author")
            put(PodDBAdapter.KEY_LANGUAGE, "en")
            put(PodDBAdapter.KEY_TYPE, "rss")
            put(PodDBAdapter.KEY_FEED_IDENTIFIER, "feed-id-full")
            put(PodDBAdapter.KEY_IMAGE_URL, "https://full.example/image.jpg")
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_FEEDS, values)

        val cursor = adapter.allFeedsCursor
        var found = false
        while (cursor.moveToNext()) {
            val title = cursor.getString(cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_TITLE))
            if (title == "Full Field Feed") {
                found = true
                assertEquals("https://full.example",
                    cursor.getString(cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_LINK)))
                assertEquals("Test Author",
                    cursor.getString(cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_AUTHOR)))
                assertEquals("rss",
                    cursor.getString(cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_TYPE)))
                assertEquals("feed-id-full",
                    cursor.getString(cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_FEED_IDENTIFIER)))
                break
            }
        }
        cursor.close()
        assertTrue("Inserted feed should be retrievable", found)
    }

    @Test
    fun feedsTable_updateState_shouldPersist() {
        val feedId = insertFeed("State Feed", "https://state.example/feed.xml")

        // Update feed state to ARCHIVED
        adapter.setFeedState(feedId, 2) // Feed.STATE_ARCHIVED

        val cursor = adapter.allFeedsCursor
        var state = -1
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(PodDBAdapter.SELECT_KEY_FEED_ID))
            if (id == feedId) {
                state = cursor.getInt(cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_STATE))
                break
            }
        }
        cursor.close()
        assertEquals("Feed state should be ARCHIVED (2)", 2, state)
    }

    @Test
    fun feedsTable_setCustomTitle_shouldPersist() {
        val feedId = insertFeed("Title Feed", "https://title.example/feed.xml")
        adapter.setFeedCustomTitle(feedId, "My Custom Title")

        val cursor = adapter.allFeedsCursor
        var customTitle: String? = null
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(PodDBAdapter.SELECT_KEY_FEED_ID))
            if (id == feedId) {
                customTitle = cursor.getString(cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_CUSTOM_TITLE))
                break
            }
        }
        cursor.close()
        assertEquals("My Custom Title", customTitle)
    }

    @Test
    fun feedsTable_insertMultiple_shouldHaveUniqueIds() {
        val id1 = insertFeed("Feed A", "https://a.example/feed.xml")
        val id2 = insertFeed("Feed B", "https://b.example/feed.xml")
        assertNotEquals("Feed IDs should be unique", id1, id2)
    }

    // -- FeedItems queries --

    @Test
    fun feedItemsTable_insertMultiple_shouldBeRetrievable() {
        val feedId = insertFeed("Multi-Item Feed", "https://multi.example/feed.xml")
        insertItem("Episode 1", feedId)
        insertItem("Episode 2", feedId)
        insertItem("Episode 3", feedId)

        val feed = Feed(
            feedId, "", "", "", "", "",
            "", "", "", "", "",
            "", "", 0L
        )
        val cursor = adapter.getItemsOfFeedCursor(
            feed,
            FeedItemFilter(),
            null, 0, Integer.MAX_VALUE
        )
        assertTrue("Items cursor should have data", cursor.count > 0)
        // At least 3 items should be present
        val count = cursor.count
        cursor.close()
        assertTrue("Should have at least 3 items", count >= 3)
    }

    @Test
    fun feedItemsTable_queryWithSortOrder_shouldReturnOrderedResults() {
        val feedId = insertFeed("Sorted Feed", "https://sorted.example/feed.xml")

        // Insert items with different pubDates
        val now = System.currentTimeMillis()
        val values1 = ContentValues().apply {
            put(PodDBAdapter.KEY_TITLE, "Oldest")
            put(PodDBAdapter.KEY_FEED, feedId)
            put(PodDBAdapter.KEY_PUBDATE, now - 86400000)
            put(PodDBAdapter.KEY_ITEM_IDENTIFIER, "iid-oldest")
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_FEED_ITEMS, values1)

        val values2 = ContentValues().apply {
            put(PodDBAdapter.KEY_TITLE, "Newest")
            put(PodDBAdapter.KEY_FEED, feedId)
            put(PodDBAdapter.KEY_PUBDATE, now)
            put(PodDBAdapter.KEY_ITEM_IDENTIFIER, "iid-newest")
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_FEED_ITEMS, values2)

        val feed = Feed(
            feedId, "", "", "", "", "",
            "", "", "", "", "",
            "", "", 0L
        )

        // Query with DATE_NEW_OLD — newest should come first
        val cursor = adapter.getItemsOfFeedCursor(
            feed,
            FeedItemFilter(),
            SortOrder.DATE_NEW_OLD,
            0, Integer.MAX_VALUE
        )
        assertTrue("Items cursor should have data", cursor.moveToFirst())
        val firstTitle = cursor.getString(cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_TITLE))
        cursor.close()
        // With DATE_NEW_OLD, the newest item should be first
        assertEquals("Newest", firstTitle)
    }

    @Test
    fun feedItemsTable_itemsByFeedId_shouldOnlyReturnThatFeedsItems() {
        val feedId1 = insertFeed("Owner Feed 1", "https://owner1.example/feed.xml")
        val feedId2 = insertFeed("Owner Feed 2", "https://owner2.example/feed.xml")
        insertItem("Feed1 Item", feedId1)
        insertItem("Feed2 Item", feedId2)

        val feed = Feed(
            feedId1, "", "", "", "", "",
            "", "", "", "", "",
            "", "", 0L
        )
        val cursor = adapter.getItemsOfFeedCursor(
            feed,
            FeedItemFilter(),
            null, 0, Integer.MAX_VALUE
        )
        var foundFeed1 = false
        var foundFeed2 = false
        while (cursor.moveToNext()) {
            val title = cursor.getString(cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_TITLE))
            if (title == "Feed1 Item") foundFeed1 = true
            if (title == "Feed2 Item") foundFeed2 = true
        }
        cursor.close()
        assertTrue("Should find Feed1's item", foundFeed1)
        assertFalse("Should NOT find Feed2's item", foundFeed2)
    }

    @Test
    fun feedItemsTable_queueInsert_shouldCreateQueueEntry() {
        val feedId = insertFeed("Queue Feed", "https://queue.example/feed.xml")
        val itemId = insertItem("Queue Episode", feedId)

        val queueValues = ContentValues().apply {
            put(PodDBAdapter.KEY_FEEDITEM, itemId)
            put(PodDBAdapter.KEY_FEED, feedId)
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_QUEUE, queueValues)

        val queueCursor = adapter.queueCursor
        assertTrue("Queue cursor should have data", queueCursor.count > 0)
        queueCursor.close()
    }
}
