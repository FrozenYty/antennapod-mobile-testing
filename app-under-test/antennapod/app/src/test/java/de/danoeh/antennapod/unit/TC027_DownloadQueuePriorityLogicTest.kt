package de.danoeh.antennapod.unit

import de.danoeh.antennapod.model.feed.Feed
import de.danoeh.antennapod.model.feed.FeedItem
import de.danoeh.antennapod.model.feed.FeedMedia
import org.junit.Assert.*
import org.junit.Test
import java.util.*

/**
 * TC-027: Download Queue Priority Logic
 *
 * Validates FeedMedia download-related model logic: download state
 * transitions, local file availability, media comparison, and
 * the data structures that support download queue ordering.
 *
 * @author Yuanbing Wang
 */
class TC027_DownloadQueuePriorityLogicTest {

    private fun createFeedAndItem(seed: Long): Pair<Feed, FeedItem> {
        val feed = Feed(
            seed, "lastModified", "Feed $seed",
            "https://example$seed.com", "Description $seed",
            "", "Author $seed", "en", "rss", "feed-$seed",
            "https://example$seed.com/image.jpg", null,
            "https://example$seed.com/feed.xml", System.currentTimeMillis()
        )
        val item = FeedItem(
            seed, "Episode $seed", "ep-$seed",
            "https://example$seed.com/ep", Date(),
            FeedItem.UNPLAYED, feed
        )
        feed.setItems(listOf(item))
        return Pair(feed, item)
    }

    @Test
    fun newlyCreatedMedia_shouldNotBeDownloaded() {
        val (_, item) = createFeedAndItem(1)
        val media = FeedMedia(item, "https://example.com/dl.mp3", 1024, "audio/mpeg")

        assertFalse("New media should not be downloaded", media.isDownloaded)
        assertEquals(0, media.downloadDate)
        assertNull(media.localFileUrl)
    }

    @Test
    fun setDownloaded_shouldMarkAsDownloadedWithTimestamp() {
        val (_, item) = createFeedAndItem(2)
        val media = FeedMedia(item, "https://example.com/dl2.mp3", 2048, "audio/mpeg")
        val timestamp = System.currentTimeMillis()

        media.setDownloaded(true, timestamp)

        assertTrue("Media should be downloaded after setDownloaded(true)", media.isDownloaded)
        assertTrue(media.downloadDate > 0)
        assertEquals(timestamp, media.downloadDate)
    }

    @Test
    fun setDownloadedFalse_shouldClearDownloadStatus() {
        val (_, item) = createFeedAndItem(3)
        val media = FeedMedia(item, "https://example.com/dl3.mp3", 3072, "audio/mpeg")

        media.setDownloaded(true, System.currentTimeMillis())
        assertTrue(media.isDownloaded)

        media.setDownloaded(false, 0)

        assertFalse("Media should not be downloaded after setDownloaded(false)", media.isDownloaded)
        assertEquals(0, media.downloadDate)
    }

    @Test
    fun mediaWithLocalFile_shouldBeAvailableOffline() {
        val (_, item) = createFeedAndItem(4)
        val media = FeedMedia(item, "https://example.com/dl4.mp3", 4096, "audio/mpeg")

        media.setLocalFileUrl("/storage/emulated/0/podcasts/ep4.mp3")
        media.setDownloaded(true, System.currentTimeMillis())

        assertTrue("Media with local file and download should be available locally",
            media.localFileAvailable())
    }

    @Test
    fun mediaWithoutLocalFile_shouldNotBeAvailableOffline() {
        val (_, item) = createFeedAndItem(5)
        val media = FeedMedia(item, "https://example.com/dl5.mp3", 5120, "audio/mpeg")

        assertFalse("Media without local file should not be locally available",
            media.localFileAvailable())
    }

    @Test
    fun setLocalFileUrlNull_shouldClearDownloadDate() {
        val (_, item) = createFeedAndItem(6)
        val media = FeedMedia(item, "https://example.com/dl6.mp3", 6144, "audio/mpeg")

        media.setLocalFileUrl("/some/path/ep6.mp3")
        media.setDownloaded(true, System.currentTimeMillis())
        assertTrue(media.localFileAvailable())

        media.setLocalFileUrl(null)

        assertNull(media.localFileUrl)
        assertEquals(0, media.downloadDate)
    }

    @Test
    fun feedMedia_differentIds_shouldNotBeEqual() {
        val (_, item) = createFeedAndItem(7)
        val media1 = FeedMedia(item, "https://example.com/a.mp3", 100, "audio/mpeg")
        media1.setId(100)
        val media2 = FeedMedia(item, "https://example.com/a.mp3", 100, "audio/mpeg")
        media2.setId(200)

        assertNotEquals(media1, media2)
    }

    @Test
    fun feedMedia_sameId_shouldBeEqual() {
        val (_, item) = createFeedAndItem(8)
        val media1 = FeedMedia(item, "https://example.com/b.mp3", 200, "audio/mpeg")
        media1.setId(300)
        val media2 = FeedMedia(item, "https://example.com/different.mp3", 999, "audio/mpeg")
        media2.setId(300)

        assertEquals(media1, media2)
    }

    @Test
    fun durationAndSize_shouldBeSettable() {
        val (_, item) = createFeedAndItem(9)
        val media = FeedMedia(item, "https://example.com/ep9.mp3", 9876543, "audio/mpeg")

        media.setDuration(180000)
        media.setSize(9876543)

        assertEquals(180000, media.duration)
        assertEquals(9876543, media.size)
    }

    @Test
    fun position_shouldTrackPlaybackProgress() {
        val (_, item) = createFeedAndItem(11)
        val media = FeedMedia(item, "https://example.com/ep11.mp3", 5000, "audio/mpeg")

        assertEquals(0, media.position)

        media.setPosition(30000)
        assertEquals(30000, media.position)
        assertTrue("Media with position > 0 should be in progress", media.isInProgress)
    }
}
