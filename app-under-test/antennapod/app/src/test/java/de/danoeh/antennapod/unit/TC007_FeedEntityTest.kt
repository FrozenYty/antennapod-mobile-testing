package de.danoeh.antennapod.unit

import de.danoeh.antennapod.model.feed.Feed
import de.danoeh.antennapod.model.feed.FeedItem
import de.danoeh.antennapod.model.feed.FeedPreferences
import de.danoeh.antennapod.model.feed.SortOrder
import de.danoeh.antennapod.model.feed.VolumeAdaptionSetting
import org.junit.Assert.*
import org.junit.Test
import java.util.Date

/**
 * TC-007: Feed Entity Field Validation
 *
 * Validates Feed entity fields, constructors, getters/setters,
 * equals/hashCode, and edge-case behaviors.
 *
 * @author Tianyu Yao
 */
class TC007_FeedEntityTest {

    @Test
    fun constructor_fullArgs_shouldSetAllFields() {
        val feed = Feed(
            1L, "lastModified", "My Title", "My Custom Title",
            "https://example.com", "A great podcast", "paymentLink",
            "John Doe", "en", "rss", "feed-id-123",
            "https://example.com/image.jpg", "/data/feed.xml",
            "https://example.com/feed.xml", 1234567890L,
            true, "https://example.com/page2", "filter_string",
            SortOrder.DATE_NEW_OLD,
            true, Feed.STATE_SUBSCRIBED
        )

        assertEquals(1L, feed.id)
        assertEquals("My Title", feed.feedTitle)
        assertEquals("My Custom Title", feed.customTitle)
        assertEquals("https://example.com", feed.link)
        assertEquals("A great podcast", feed.description)
        assertEquals("John Doe", feed.author)
        assertEquals("en", feed.language)
        assertEquals("rss", feed.type)
        assertEquals("feed-id-123", feed.feedIdentifier)
        assertEquals("https://example.com/image.jpg", feed.imageUrl)
        assertEquals("/data/feed.xml", feed.localFileUrl)
        assertEquals("https://example.com/feed.xml", feed.downloadUrl)
        assertEquals(1234567890L, feed.lastRefreshAttempt)
        assertTrue(feed.isPaged)
        assertEquals("https://example.com/page2", feed.nextPageLink)
        assertTrue(feed.hasLastUpdateFailed())
        assertEquals(Feed.STATE_SUBSCRIBED, feed.state)
    }

    @Test
    fun constructor_testPurpose_shouldSetRequiredFields() {
        val feed = Feed(
            1L, "lastModified", "Test Title",
            "https://example.com", "Description", "payment",
            "Author", "en", "rss", "feed-id",
            "https://img.url", "/file.xml",
            "https://download.url", 1000L
        )

        assertEquals(1L, feed.id)
        assertEquals("Test Title", feed.feedTitle)
        assertEquals("https://example.com", feed.link)
        assertEquals("Description", feed.description)
        assertEquals("Author", feed.author)
        assertEquals("en", feed.language)
        assertEquals("rss", feed.type)
        assertEquals("feed-id", feed.feedIdentifier)
        assertNull(feed.customTitle)
        assertEquals(Feed.STATE_SUBSCRIBED, feed.state)
    }

    @Test
    fun getTitle_withCustomTitle_shouldReturnCustomTitle() {
        val feed = Feed(
            1L, "lastModified", "Feed Title",
            "https://example.com", "Desc", "pay",
            "Author", "en", "rss", "fid",
            "https://img", "/file", "https://dl", 0L
        )
        feed.customTitle = "Custom"

        assertEquals("Custom", feed.title)
    }

    @Test
    fun getTitle_withoutCustomTitle_shouldReturnFeedTitle() {
        val feed = Feed(
            1L, "lastModified", "Feed Title",
            "https://example.com", "Desc", "pay",
            "Author", "en", "rss", "fid",
            "https://img", "/file", "https://dl", 0L
        )

        assertEquals("Feed Title", feed.title)
    }

    @Test
    fun getIdentifyingValue_withFeedIdentifier_shouldReturnFeedIdentifier() {
        val feed = Feed(
            1L, "lastModified", "Title",
            "https://example.com", "Desc", "pay",
            "Author", "en", "rss", "feed-id-abc",
            "https://img", "/file", "https://dl", 0L
        )

        assertEquals("feed-id-abc", feed.identifyingValue)
    }

    @Test
    fun getIdentifyingValue_withoutFeedIdentifier_shouldReturnDownloadUrl() {
        val feed = Feed(
            1L, "lastModified", "Title",
            "https://example.com", "Desc", "pay",
            "Author", "en", "rss", null,
            "https://img", "/file", "https://download.url", 0L
        )

        assertEquals("https://download.url", feed.identifyingValue)
    }

    @Test
    fun equals_sameId_shouldBeEqual() {
        val feed1 = Feed(
            42L, "lm", "Title", "https://ex.com", "Desc",
            "pay", "Author", "en", "rss", "fid",
            "https://img", "/file", "https://dl", 0L
        )
        val feed2 = Feed(
            42L, "different", "Different", "https://diff.com", "Other",
            "p2", "A2", "de", "atom", "fid2",
            "https://img2", "/f2", "https://dl2", 999L
        )

        assertEquals(feed1, feed2)
        assertEquals(feed1.hashCode(), feed2.hashCode())
    }

    @Test
    fun equals_differentId_shouldNotBeEqual() {
        val feed1 = Feed(
            1L, "lm", "Title", "https://ex.com", "Desc",
            "pay", "Author", "en", "rss", "fid",
            "https://img", "/file", "https://dl", 0L
        )
        val feed2 = Feed(
            2L, "lm", "Title", "https://ex.com", "Desc",
            "pay", "Author", "en", "rss", "fid",
            "https://img", "/file", "https://dl", 0L
        )

        assertNotEquals(feed1, feed2)
    }

    @Test
    fun setSortOrder_invalidScope_shouldThrowException() {
        val feed = Feed(
            1L, "lm", "Title", "https://ex.com", "Desc",
            "pay", "Author", "en", "rss", "fid",
            "https://img", "/file", "https://dl", 0L
        )

        try {
            // RANDOM has INTER_FEED scope which is invalid for Feed.setSortOrder
            feed.sortOrder = SortOrder.RANDOM
            fail("Expected IllegalArgumentException for invalid SortOrder scope")
        } catch (e: IllegalArgumentException) {
            // Expected
        }
    }

    @Test
    fun setSortOrder_validScope_shouldAccept() {
        val feed = Feed(
            1L, "lm", "Title", "https://ex.com", "Desc",
            "pay", "Author", "en", "rss", "fid",
            "https://img", "/file", "https://dl", 0L
        )
        val order = SortOrder.DATE_NEW_OLD
        feed.sortOrder = order
        assertEquals(order, feed.sortOrder)
    }

    @Test
    fun setCustomTitle_sameAsFeedTitle_shouldSetToNull() {
        val feed = Feed(
            1L, "lm", "Feed Title", "https://ex.com", "Desc",
            "pay", "Author", "en", "rss", "fid",
            "https://img", "/file", "https://dl", 0L
        )
        feed.customTitle = "Feed Title"
        assertNull(feed.customTitle)
    }

    @Test
    fun setCustomTitle_differentFromFeedTitle_shouldStore() {
        val feed = Feed(
            1L, "lm", "Feed Title", "https://ex.com", "Desc",
            "pay", "Author", "en", "rss", "fid",
            "https://img", "/file", "https://dl", 0L
        )
        feed.customTitle = "My Custom"
        assertEquals("My Custom", feed.customTitle)
    }

    @Test
    fun setId_shouldSetPreferencesFeedId() {
        val feed = Feed(
            1L, "lm", "Title", "https://ex.com", "Desc",
            "pay", "Author", "en", "rss", "fid",
            "https://img", "/file", "https://dl", 0L
        )
        feed.preferences = FeedPreferences(
            1L, FeedPreferences.AutoDownloadSetting.GLOBAL,
            FeedPreferences.AutoDeleteAction.GLOBAL,
            VolumeAdaptionSetting.OFF,
            FeedPreferences.NewEpisodesAction.GLOBAL,
            null, null
        )

        feed.id = 99L
        assertEquals(99L, feed.id)
        assertEquals(99L, feed.preferences.feedID)
    }

    @Test
    fun getHumanReadableIdentifier_withCustomTitle_shouldReturnCustomTitle() {
        val feed = Feed(
            1L, "lm", "Feed Title", "https://ex.com", "Desc",
            "pay", "Author", "en", "rss", "fid",
            "https://img", "/file", "https://dl", 0L
        )
        feed.customTitle = "My Custom Title"

        assertEquals("My Custom Title", feed.humanReadableIdentifier)
    }

    @Test
    fun isLocalFeed_withLocalPrefix_shouldReturnTrue() {
        val feed = Feed(
            1L, "lm", "Local Feed", "https://ex.com", "Desc",
            "pay", "Author", "en", "rss", "fid",
            "https://img", "/file", Feed.PREFIX_LOCAL_FOLDER + "test", 0L
        )
        assertTrue(feed.isLocalFeed)
    }

    @Test
    fun isLocalFeed_withHttpUrl_shouldReturnFalse() {
        val feed = Feed(
            1L, "lm", "Remote Feed", "https://ex.com", "Desc",
            "pay", "Author", "en", "rss", "fid",
            "https://img", "/file", "https://example.com/feed.xml", 0L
        )
        assertFalse(feed.isLocalFeed)
    }

    @Test
    fun setItems_shouldReplaceItemList() {
        val feed = Feed(
            1L, "lm", "Title", "https://ex.com", "Desc",
            "pay", "Author", "en", "rss", "fid",
            "https://img", "/file", "https://dl", 0L
        )
        val item1 = FeedItem(
            1L, "Item 1", "iid-1", "https://item1.com", Date(), FeedItem.UNPLAYED, feed
        )
        feed.items = listOf(item1)

        assertEquals(1, feed.items.size)
        assertEquals("Item 1", feed.items[0].title)
    }
}
