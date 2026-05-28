package de.danoeh.antennapod.unit

import de.danoeh.antennapod.model.feed.Feed
import de.danoeh.antennapod.model.feed.FeedItem
import de.danoeh.antennapod.model.feed.FeedMedia
import org.junit.Assert.*
import org.junit.Test
import java.util.Date

/**
 * TC-008: FeedItem & FeedMedia Entity Validation
 *
 * Validates FeedItem and FeedMedia fields, constructors, state transitions,
 * and cross-referencing between the two entities.
 *
 * @author Tianyu Yao
 */
class TC008_FeedItemFeedMediaTest {

    private fun createTestFeed(): Feed {
        return Feed(
            1L, "lm", "Test Feed", "https://feed.com", "Desc",
            "pay", "Author", "en", "rss", "feed-id",
            "https://img", "/file", "https://dl.feed", 0L
        )
    }

    // ---- FeedItem Tests ----

    @Test
    fun feedItem_constructor_shouldSetAllFields() {
        val feed = createTestFeed()
        val pubDate = Date(1609459200000L)

        val item = FeedItem(100L, "Episode 1", "item-id-001", "https://ep.com", pubDate, FeedItem.UNPLAYED, feed)

        assertEquals(100L, item.id)
        assertEquals("Episode 1", item.title)
        assertEquals("item-id-001", item.itemIdentifier)
        assertEquals("https://ep.com", item.link)
        assertEquals(pubDate, item.pubDate)
        assertEquals(FeedItem.UNPLAYED, item.playState)
        assertEquals(feed, item.feed)
        assertFalse(item.isPlayed)
        assertFalse(item.isNew)
    }

    @Test
    fun feedItem_defaultConstructor_shouldHaveUnplayedState() {
        val item = FeedItem()
        assertEquals(FeedItem.UNPLAYED, item.playState)
        assertFalse(item.hasMedia())
    }

    @Test
    fun feedItem_setPlayed_shouldUpdateState() {
        val item = FeedItem()
        item.setPlayed(true)
        assertEquals(FeedItem.PLAYED, item.playState)
        assertTrue(item.isPlayed)
    }

    @Test
    fun feedItem_setPlayedFalse_shouldSetUnplayed() {
        val item = FeedItem()
        item.setPlayed(true)
        item.setPlayed(false)
        assertEquals(FeedItem.UNPLAYED, item.playState)
    }

    @Test
    fun feedItem_setNew_shouldSetNewState() {
        val item = FeedItem()
        item.setNew()
        assertEquals(FeedItem.NEW, item.playState)
        assertTrue(item.isNew)
    }

    @Test
    fun feedItem_getIdentifyingValue_withItemIdentifier_shouldReturnIdentifier() {
        val feed = createTestFeed()
        val item = FeedItem(1L, "Title", "guid-123", "https://link.com", Date(), FeedItem.UNPLAYED, feed)

        assertEquals("guid-123", item.identifyingValue)
    }

    @Test
    fun feedItem_getIdentifyingValue_withoutIdentifier_shouldReturnTitle() {
        val feed = createTestFeed()
        val item = FeedItem(1L, "Episode Title", null, "https://link.com", Date(), FeedItem.UNPLAYED, feed)

        assertEquals("Episode Title", item.identifyingValue)
    }

    @Test
    fun feedItem_getIdentifyingValue_withMediaUrl_shouldReturnMediaUrl() {
        val feed = createTestFeed()
        val item = FeedItem(1L, null, null, "https://link.com", Date(), FeedItem.UNPLAYED, feed)
        val media = FeedMedia(item, "https://media.url/audio.mp3", 1024L, "audio/mpeg")
        item.media = media

        assertEquals("https://media.url/audio.mp3", item.identifyingValue)
    }

    @Test
    fun feedItem_equals_sameId_shouldBeEqual() {
        val feed = createTestFeed()
        val item1 = FeedItem(42L, "Ep1", "iid1", "https://a.com", Date(), FeedItem.UNPLAYED, feed)
        val item2 = FeedItem(42L, "Ep2", "iid2", "https://b.com", Date(), FeedItem.PLAYED, feed)

        assertEquals(item1, item2)
        assertEquals(item1.hashCode(), item2.hashCode())
    }

    @Test
    fun feedItem_equals_differentId_shouldNotBeEqual() {
        val feed = createTestFeed()
        val item1 = FeedItem(1L, "Ep", "iid", "https://a.com", Date(), FeedItem.UNPLAYED, feed)
        val item2 = FeedItem(2L, "Ep", "iid", "https://a.com", Date(), FeedItem.UNPLAYED, feed)

        assertNotEquals(item1, item2)
    }

    @Test
    fun feedItem_setMedia_shouldSetBidirectionalReference() {
        val feed = createTestFeed()
        val item = FeedItem(1L, "Episode", "iid", "https://ep.com", Date(), FeedItem.UNPLAYED, feed)
        val media = FeedMedia(item, "https://dl.mp3", 2048L, "audio/mpeg")

        item.media = media
        assertNotNull(item.media)
        assertEquals(media, item.media)
        assertEquals(item, media.item)
    }

    @Test
    fun feedItem_isDownloaded_withDownloadedMedia_shouldReturnTrue() {
        val feed = createTestFeed()
        val item = FeedItem(1L, "Episode", "iid", "https://ep.com", Date(), FeedItem.UNPLAYED, feed)
        val media = FeedMedia(item, "https://dl.mp3", 2048L, "audio/mpeg")
        item.media = media
        media.setDownloaded(true, System.currentTimeMillis())

        assertTrue(item.isDownloaded)
    }

    @Test
    fun feedItem_isDownloaded_withoutMedia_shouldReturnFalse() {
        val feed = createTestFeed()
        val item = FeedItem(1L, "Episode", "iid", "https://ep.com", Date(), FeedItem.UNPLAYED, feed)

        assertFalse(item.isDownloaded)
    }

    @Test
    fun feedItem_tagManagement_shouldWorkCorrectly() {
        val feed = createTestFeed()
        val item = FeedItem(1L, "Episode", "iid", "https://ep.com", Date(), FeedItem.UNPLAYED, feed)

        item.addTag(FeedItem.TAG_QUEUE)
        assertTrue(item.isTagged(FeedItem.TAG_QUEUE))

        item.addTag(FeedItem.TAG_FAVORITE)
        assertTrue(item.isTagged(FeedItem.TAG_FAVORITE))

        item.removeTag(FeedItem.TAG_QUEUE)
        assertFalse(item.isTagged(FeedItem.TAG_QUEUE))
        assertTrue(item.isTagged(FeedItem.TAG_FAVORITE))
    }

    @Test
    fun feedItem_disableAutoDownload_shouldDisable() {
        val feed = createTestFeed()
        val item = FeedItem(1L, "Episode", "iid", "https://ep.com", Date(), FeedItem.UNPLAYED, feed)

        assertTrue(item.isAutoDownloadEnabled)
        item.disableAutoDownload()
        assertFalse(item.isAutoDownloadEnabled)
    }

    @Test
    fun feedItem_isInProgress_withMediaInProgress_shouldReturnTrue() {
        val feed = createTestFeed()
        val item = FeedItem(1L, "Episode", "iid", "https://ep.com", Date(), FeedItem.UNPLAYED, feed)
        val media = FeedMedia(1L, item, 3600, 600, 0L, "audio/mpeg", null, "https://dl.mp3", 0L, null, 0, 0L)
        item.media = media

        assertTrue(item.isInProgress)
    }

    @Test
    fun feedItem_isInProgress_withoutMedia_shouldReturnFalse() {
        val feed = createTestFeed()
        val item = FeedItem(1L, "Episode", "iid", "https://ep.com", Date(), FeedItem.UNPLAYED, feed)

        assertFalse(item.isInProgress)
    }

    @Test
    fun feedItem_getPubDate_shouldReturnDefensiveCopy() {
        val date = Date(1609459200000L)
        val feed = createTestFeed()
        val item = FeedItem(1L, "Episode", "iid", "https://ep.com", date, FeedItem.UNPLAYED, feed)

        val retrieved = item.pubDate
        assertNotNull(retrieved)
        assertEquals(date, retrieved)

        retrieved!!.time = 9999999999999L
        assertNotEquals(retrieved.time, item.pubDate?.time)
    }

    @Test
    fun feedItem_setPubDate_shouldStoreDefensiveCopy() {
        val date = Date(1609459200000L)
        val feed = createTestFeed()
        val item = FeedItem(1L, "Episode", "iid", "https://ep.com", null, FeedItem.UNPLAYED, feed)

        item.pubDate = date
        date.time = 9999999999999L
        assertNotEquals(date.time, item.pubDate?.time)
    }

    @Test
    fun feedItem_getImageLocation_withItemImage_shouldReturnItemImage() {
        val feed = createTestFeed()
        val item = FeedItem(1L, "Episode", "iid", "https://ep.com", Date(), FeedItem.UNPLAYED, feed)
        item.imageUrl = "https://item.img/cover.jpg"

        assertEquals("https://item.img/cover.jpg", item.imageLocation)
    }

    @Test
    fun feedItem_getImageLocation_fallbackToFeedImage_shouldReturnFeedImage() {
        val feed = createTestFeed()
        val item = FeedItem(1L, "Episode", "iid", "https://ep.com", Date(), FeedItem.UNPLAYED, feed)

        assertEquals("https://img", item.imageLocation)
    }

    // ---- FeedMedia Tests ----

    @Test
    fun feedMedia_constructor_shouldSetFields() {
        val feed = createTestFeed()
        val item = FeedItem(1L, "Episode", "iid", "https://ep.com", Date(), FeedItem.UNPLAYED, feed)
        val media = FeedMedia(item, "https://audio.mp3", 4096L, "audio/mpeg")

        assertEquals("https://audio.mp3", media.downloadUrl)
        assertEquals(4096L, media.size)
        assertEquals("audio/mpeg", media.mimeType)
        assertEquals(item, media.item)
    }

    @Test
    fun feedMedia_fullConstructor_shouldSetAllFields() {
        val feed = createTestFeed()
        val item = FeedItem(1L, "Episode", "iid", "https://ep.com", Date(), FeedItem.UNPLAYED, feed)
        val lastPlayed = Date(1609459200000L)

        val media = FeedMedia(
            10L, item, 1800, 300, 2048L, "audio/mpeg",
            "/data/episode.mp3", "https://audio.mp3", 1609459200L,
            lastPlayed, 600, 1609459200000L
        )

        assertEquals(10L, media.id)
        assertEquals(1800, media.duration)
        assertEquals(300, media.position)
        assertEquals(2048L, media.size)
        assertEquals("audio/mpeg", media.mimeType)
        assertEquals("/data/episode.mp3", media.localFileUrl)
        assertEquals("https://audio.mp3", media.downloadUrl)
        assertEquals(600, media.playedDuration)
        assertEquals(item, media.item)
    }

    @Test
    fun feedMedia_getHumanReadableIdentifier_withItemTitle_shouldReturnTitle() {
        val feed = createTestFeed()
        val item = FeedItem(1L, "Awesome Episode", "iid", "https://ep.com", Date(), FeedItem.UNPLAYED, feed)
        val media = FeedMedia(item, "https://audio.mp3", 1024L, "audio/mpeg")

        assertEquals("Awesome Episode", media.humanReadableIdentifier)
    }

    @Test
    fun feedMedia_isDownloaded_shouldReturnTrueWhenDownloadDatePositive() {
        val feed = createTestFeed()
        val item = FeedItem(1L, "Episode", "iid", "https://ep.com", Date(), FeedItem.UNPLAYED, feed)
        val media = FeedMedia(item, "https://audio.mp3", 1024L, "audio/mpeg")
        media.setDownloaded(true, 1609459200000L)

        assertTrue(media.isDownloaded)
    }

    @Test
    fun feedMedia_isDownloaded_shouldReturnFalseByDefault() {
        val feed = createTestFeed()
        val item = FeedItem(1L, "Episode", "iid", "https://ep.com", Date(), FeedItem.UNPLAYED, feed)
        val media = FeedMedia(item, "https://audio.mp3", 1024L, "audio/mpeg")

        assertFalse(media.isDownloaded)
    }

    @Test
    fun feedMedia_setPosition_shouldUpdatePosition() {
        val feed = createTestFeed()
        val item = FeedItem(1L, "Episode", "iid", "https://ep.com", Date(), FeedItem.UNPLAYED, feed)
        val media = FeedMedia(item, "https://audio.mp3", 1024L, "audio/mpeg")

        media.position = 500
        assertEquals(500, media.position)
    }

    @Test
    fun feedMedia_fileExists_withValidFile_shouldCheckExistence() {
        val feed = createTestFeed()
        val item = FeedItem(1L, "Episode", "iid", "https://ep.com", Date(), FeedItem.UNPLAYED, feed)
        val media = FeedMedia(item, "https://audio.mp3", 1024L, "audio/mpeg")
        media.setLocalFileUrl("/nonexistent/file.mp3")
        media.setDownloaded(true, System.currentTimeMillis())

        assertFalse(media.fileExists())
    }

    @Test
    fun feedMedia_equals_sameId_shouldBeEqual() {
        val feed = createTestFeed()
        val item = FeedItem(1L, "Episode", "iid", "https://ep.com", Date(), FeedItem.UNPLAYED, feed)
        val media1 = FeedMedia(42L, item, 100, 0, 1024L, "audio/mpeg", null, "https://a.mp3", 0L, null, 0, 0L)
        val media2 = FeedMedia(42L, item, 200, 50, 2048L, "audio/ogg", "/file", "https://b.mp3", 0L, null, 100, 0L)

        assertEquals(media1, media2)
    }

    @Test
    fun feedMedia_equals_differentId_shouldNotBeEqual() {
        val feed = createTestFeed()
        val item = FeedItem(1L, "Episode", "iid", "https://ep.com", Date(), FeedItem.UNPLAYED, feed)
        val media1 = FeedMedia(1L, item, 100, 0, 1024L, "audio/mpeg", null, "https://a.mp3", 0L, null, 0, 0L)
        val media2 = FeedMedia(2L, item, 100, 0, 1024L, "audio/mpeg", null, "https://a.mp3", 0L, null, 0, 0L)

        assertNotEquals(media1, media2)
    }

    @Test
    fun feedMedia_isInProgress_withPositivePosition_shouldReturnTrue() {
        val feed = createTestFeed()
        val item = FeedItem(1L, "Episode", "iid", "https://ep.com", Date(), FeedItem.UNPLAYED, feed)
        val media = FeedMedia(1L, item, 3600, 300, 0L, "audio/mpeg", null, "https://a.mp3", 0L, null, 0, 0L)

        assertTrue(media.isInProgress)
    }

    @Test
    fun feedMedia_isInProgress_atStart_shouldReturnFalse() {
        val feed = createTestFeed()
        val item = FeedItem(1L, "Episode", "iid", "https://ep.com", Date(), FeedItem.UNPLAYED, feed)
        val media = FeedMedia(1L, item, 3600, 0, 0L, "audio/mpeg", null, "https://a.mp3", 0L, null, 0, 0L)

        assertFalse(media.isInProgress)
    }

    @Test
    fun feedMedia_localFileAvailable_downloadedWithFile_shouldReturnTrue() {
        val feed = createTestFeed()
        val item = FeedItem(1L, "Episode", "iid", "https://ep.com", Date(), FeedItem.UNPLAYED, feed)
        val media = FeedMedia(item, "https://audio.mp3", 1024L, "audio/mpeg")
        media.setLocalFileUrl("/data/file.mp3")
        media.setDownloaded(true, System.currentTimeMillis())

        assertTrue(media.localFileAvailable())
    }

    @Test
    fun feedMedia_localFileAvailable_notDownloaded_shouldReturnFalse() {
        val feed = createTestFeed()
        val item = FeedItem(1L, "Episode", "iid", "https://ep.com", Date(), FeedItem.UNPLAYED, feed)
        val media = FeedMedia(item, "https://audio.mp3", 1024L, "audio/mpeg")

        assertFalse(media.localFileAvailable())
    }
}
