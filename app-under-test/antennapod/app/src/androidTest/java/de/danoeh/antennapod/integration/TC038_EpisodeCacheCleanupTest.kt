package de.danoeh.antennapod.integration

import android.content.ContentValues
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.danoeh.antennapod.model.feed.Feed
import de.danoeh.antennapod.model.feed.FeedItem
import de.danoeh.antennapod.model.feed.FeedItemFilter
import de.danoeh.antennapod.model.feed.FeedMedia
import de.danoeh.antennapod.storage.database.PodDBAdapter
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

/**
 * TC-038: Episode Cache Table Cleanup
 *
 * Verifies database cleanup behavior for download logs, queue rows, feed
 * items, and associated FeedMedia cache metadata.
 *
 * @author Member Four
 */
@RunWith(AndroidJUnit4::class)
class TC038_EpisodeCacheCleanupTest {

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
    fun clearDownloadLog_shouldRemoveAllRows() {
        insertDownloadLog(1L, Feed.FEEDFILETYPE_FEED, "Feed log")
        insertDownloadLog(2L, FeedMedia.FEEDFILETYPE_FEEDMEDIA, "Media log")

        assertTrue(downloadLogCount() >= 2)

        adapter.clearDownloadLog()

        assertEquals(0, downloadLogCount())
    }

    @Test
    fun clearOldDownloadLog_shouldKeepRecentRowsOnly() {
        val oldDate = System.currentTimeMillis() - 8L * 24L * 3600L * 1000L
        val recentDate = System.currentTimeMillis()
        insertDownloadLog(11L, Feed.FEEDFILETYPE_FEED, "Old log", oldDate)
        insertDownloadLog(12L, Feed.FEEDFILETYPE_FEED, "Recent log", recentDate)

        adapter.clearOldDownloadLog()

        val titles = downloadLogTitles()
        assertEquals(listOf("Recent log"), titles)
    }

    @Test
    fun clearQueue_shouldRemoveQueuedEpisodeRows() {
        val feed = insertFeed(101L, "Queue Cleanup Feed")
        val item = insertFeedItem(201L, feed, "Queued Episode")
        insertQueueRow(item.id, feed.id)

        assertTrue(queueCount() > 0)

        adapter.clearQueue()

        assertEquals(0, queueCount())
    }

    @Test
    fun removeFeedItems_shouldDeleteItemMediaAndRelatedDownloadLog() {
        val feed = insertFeed(102L, "Cache Cleanup Feed")
        val item = insertFeedItem(202L, feed, "Cached Episode")
        val media = insertFeedMedia(302L, item)
        item.media = media
        insertDownloadLog(media.id, FeedMedia.FEEDFILETYPE_FEEDMEDIA, "Cached media log")

        adapter.removeFeedItems(listOf(item))

        assertEquals(0, feedItemCount(feed))
        assertEquals(0, mediaCount(media.id))
        assertEquals(0, downloadLogCount(FeedMedia.FEEDFILETYPE_FEEDMEDIA, media.id))
    }

    private fun insertFeed(id: Long, title: String): Feed {
        val values = ContentValues().apply {
            put(PodDBAdapter.KEY_ID, id)
            put(PodDBAdapter.KEY_TITLE, title)
            put(PodDBAdapter.KEY_DOWNLOAD_URL, "https://cleanup.example/feed$id.xml")
            put(PodDBAdapter.KEY_LINK, "https://cleanup.example")
            put(PodDBAdapter.KEY_STATE, Feed.STATE_SUBSCRIBED)
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_FEEDS, values)
        return Feed(id, "", title, "https://cleanup.example", "", "", "", "",
            "rss", "feed-$id", "", "", "https://cleanup.example/feed$id.xml", 0L)
    }

    private fun insertFeedItem(id: Long, feed: Feed, title: String): FeedItem {
        val values = ContentValues().apply {
            put(PodDBAdapter.KEY_ID, id)
            put(PodDBAdapter.KEY_TITLE, title)
            put(PodDBAdapter.KEY_FEED, feed.id)
            put(PodDBAdapter.KEY_LINK, "https://cleanup.example/episode$id")
            put(PodDBAdapter.KEY_PUBDATE, System.currentTimeMillis())
            put(PodDBAdapter.KEY_ITEM_IDENTIFIER, "episode-$id")
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_FEED_ITEMS, values)
        return FeedItem(id, title, "episode-$id", "https://cleanup.example/episode$id",
            Date(), FeedItem.UNPLAYED, feed)
    }

    private fun insertFeedMedia(id: Long, item: FeedItem): FeedMedia {
        val values = ContentValues().apply {
            put(PodDBAdapter.KEY_ID, id)
            put(PodDBAdapter.KEY_FEEDITEM, item.id)
            put(PodDBAdapter.KEY_DOWNLOAD_URL, "https://cleanup.example/media$id.mp3")
            put(PodDBAdapter.KEY_FILE_URL, "/tmp/media$id.mp3")
            put(PodDBAdapter.KEY_MIME_TYPE, "audio/mpeg")
            put(PodDBAdapter.KEY_SIZE, 1024L)
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_FEED_MEDIA, values)
        return FeedMedia(id, item, 0, 0, 1024L, "audio/mpeg",
            "/tmp/media$id.mp3", "https://cleanup.example/media$id.mp3", 0L, null, 0, 0L)
    }

    private fun insertQueueRow(itemId: Long, feedId: Long) {
        val values = ContentValues().apply {
            put(PodDBAdapter.KEY_FEEDITEM, itemId)
            put(PodDBAdapter.KEY_FEED, feedId)
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_QUEUE, values)
    }

    private fun insertDownloadLog(
        feedFileId: Long,
        feedFileType: Int,
        title: String,
        completionDate: Long = System.currentTimeMillis()
    ) {
        val values = ContentValues().apply {
            put(PodDBAdapter.KEY_FEEDFILE, feedFileId)
            put(PodDBAdapter.KEY_FEEDFILETYPE, feedFileType)
            put(PodDBAdapter.KEY_REASON, 0)
            put(PodDBAdapter.KEY_SUCCESSFUL, 1)
            put(PodDBAdapter.KEY_COMPLETION_DATE, completionDate)
            put(PodDBAdapter.KEY_DOWNLOADSTATUS_TITLE, title)
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_DOWNLOAD_LOG, values)
    }

    private fun downloadLogCount(): Int {
        val cursor = adapter.getDownloadLogCursor(50)
        val count = cursor.count
        cursor.close()
        return count
    }

    private fun downloadLogCount(feedFileType: Int, feedFileId: Long): Int {
        val cursor = adapter.getDownloadLog(feedFileType, feedFileId, 50)
        val count = cursor.count
        cursor.close()
        return count
    }

    private fun downloadLogTitles(): List<String> {
        val cursor = adapter.getDownloadLogCursor(50)
        val titles = mutableListOf<String>()
        while (cursor.moveToNext()) {
            titles.add(cursor.getString(cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_DOWNLOADSTATUS_TITLE)))
        }
        cursor.close()
        return titles
    }

    private fun queueCount(): Int {
        val cursor = adapter.queueCursor
        val count = cursor.count
        cursor.close()
        return count
    }

    private fun feedItemCount(feed: Feed): Int {
        val cursor = adapter.getItemsOfFeedCursor(feed, FeedItemFilter(), null, 0, Integer.MAX_VALUE)
        val count = cursor.count
        cursor.close()
        return count
    }

    private fun mediaCount(mediaId: Long): Int {
        val cursor = adapter.getFeedItemFromMediaIdCursor(mediaId)
        val count = cursor.count
        cursor.close()
        return count
    }
}
