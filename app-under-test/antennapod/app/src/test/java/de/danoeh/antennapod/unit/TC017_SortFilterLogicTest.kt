package de.danoeh.antennapod.unit

import de.danoeh.antennapod.model.feed.Feed
import de.danoeh.antennapod.model.feed.FeedItem
import de.danoeh.antennapod.model.feed.FeedItemFilter
import de.danoeh.antennapod.model.feed.FeedOrder
import de.danoeh.antennapod.model.feed.SortOrder
import de.danoeh.antennapod.model.feed.SubscriptionsFilter
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Date

/**
 * TC-017: Subscription Sort & Filter Logic
 *
 * Validates feed sort order, feed item filtering, and subscription
 * filter logic used in the subscriptions screen. Tests enum round-trips,
 * scope validation, and filter predicate behavior.
 *
 * Uses RobolectricTestRunner to mock Android TextUtils calls.
 *
 * @author Jianheng Sun
 */
@RunWith(RobolectricTestRunner::class)
class TC017_SortFilterLogicTest {

    // ---- FeedOrder ----

    @Test
    fun feedOrder_fromOrdinal_counter_shouldReturnCounter() {
        assertEquals(FeedOrder.COUNTER, FeedOrder.fromOrdinal(0))
    }

    @Test
    fun feedOrder_fromOrdinal_alphabetical_shouldReturnAlphabetical() {
        assertEquals(FeedOrder.ALPHABETICAL, FeedOrder.fromOrdinal(1))
    }

    @Test
    fun feedOrder_fromOrdinal_mostRecent_shouldReturnMostRecent() {
        assertEquals(FeedOrder.MOST_RECENT_EPISODE, FeedOrder.fromOrdinal(2))
    }

    @Test
    fun feedOrder_fromOrdinal_mostPlayed_shouldReturnMostPlayed() {
        assertEquals(FeedOrder.MOST_PLAYED, FeedOrder.fromOrdinal(3))
    }

    @Test
    fun feedOrder_idRoundTrip_shouldBeConsistent() {
        for (order in FeedOrder.values()) {
            assertEquals(order, FeedOrder.fromOrdinal(order.id))
        }
    }

    // ---- SortOrder scope ----

    @Test
    fun sortOrder_dateNewOld_shouldBeIntraFeedScope() {
        assertEquals(SortOrder.Scope.INTRA_FEED, SortOrder.DATE_NEW_OLD.scope)
    }

    @Test
    fun sortOrder_feedTitleAZ_shouldBeInterFeedScope() {
        assertEquals(SortOrder.Scope.INTER_FEED, SortOrder.FEED_TITLE_A_Z.scope)
    }

    @Test
    fun sortOrder_random_shouldBeInterFeedScope() {
        assertEquals(SortOrder.Scope.INTER_FEED, SortOrder.RANDOM.scope)
    }

    @Test
    fun sortOrder_fromCodeString_dateNewOld_shouldParse() {
        assertEquals(SortOrder.DATE_NEW_OLD, SortOrder.fromCodeString("2"))
    }

    @Test
    fun sortOrder_fromCodeString_invalidCode_shouldThrowException() {
        try {
            SortOrder.fromCodeString("UnknownValue")
            fail("Expected IllegalArgumentException for non-numeric code")
        } catch (e: IllegalArgumentException) {
            // Expected
        }
    }

    @Test
    fun sortOrder_toCodeString_shouldRoundTrip() {
        val code = SortOrder.toCodeString(SortOrder.EPISODE_TITLE_A_Z)
        val parsed = SortOrder.fromCodeString(code)
        assertEquals(SortOrder.EPISODE_TITLE_A_Z, parsed)
    }

    // ---- FeedItemFilter ----

    @Test
    fun feedItemFilter_unplayed_shouldMatchUnplayedItem() {
        val feed = createTestFeed(1L)
        val item = FeedItem(1L, "Test", "iid", "https://link", Date(), FeedItem.UNPLAYED, feed)
        val filter = FeedItemFilter(FeedItemFilter.UNPLAYED)
        assertTrue(filter.matches(item))
    }

    @Test
    fun feedItemFilter_played_shouldMatchPlayedItem() {
        val feed = createTestFeed(1L)
        val item = FeedItem(1L, "Test", "iid", "https://link", Date(), FeedItem.PLAYED, feed)
        val filter = FeedItemFilter(FeedItemFilter.PLAYED)
        assertTrue(filter.matches(item))
    }

    @Test
    fun feedItemFilter_played_shouldNotMatchUnplayedItem() {
        val feed = createTestFeed(1L)
        val item = FeedItem(1L, "Test", "iid", "https://link", Date(), FeedItem.UNPLAYED, feed)
        val filter = FeedItemFilter(FeedItemFilter.PLAYED)
        assertFalse(filter.matches(item))
    }

    @Test
    fun feedItemFilter_new_shouldMatchNewItem() {
        val feed = createTestFeed(1L)
        val item = FeedItem(1L, "Test", "iid", "https://link", Date(), FeedItem.UNPLAYED, feed, false)
        // A newly created item with NEW-like state
        val filter = FeedItemFilter(FeedItemFilter.NEW)
        // Items created with NEW state (-1) are considered "new"
        val newItem = FeedItem(1L, "New", "iid2", "https://link", Date(), FeedItem.NEW, feed)
        assertTrue(filter.matches(newItem))
    }

    @Test
    fun feedItemFilter_queue_shouldMatchQueuedItem() {
        val feed = createTestFeed(1L)
        val item = FeedItem(1L, "QItem", "iid", "https://link", Date(), FeedItem.UNPLAYED, feed)
        item.addTag(FeedItem.TAG_QUEUE)
        val filter = FeedItemFilter(FeedItemFilter.QUEUED)
        assertTrue(filter.matches(item))
    }

    @Test
    fun feedItemFilter_favorite_shouldMatchFavoriteItem() {
        val feed = createTestFeed(1L)
        val item = FeedItem(1L, "Fav", "iid", "https://link", Date(), FeedItem.UNPLAYED, feed)
        item.addTag(FeedItem.TAG_FAVORITE)
        val filter = FeedItemFilter(FeedItemFilter.IS_FAVORITE)
        assertTrue(filter.matches(item))
    }

    @Test
    fun feedItemFilter_hasMedia_shouldMatchItemWithMedia() {
        val feed = createTestFeed(1L)
        val item = FeedItem(1L, "HasMedia", "iid", "https://link", Date(), FeedItem.UNPLAYED, feed)
        val media = de.danoeh.antennapod.model.feed.FeedMedia(
            item, "https://example.com/ep.mp3", 1024, "audio/mpeg"
        )
        item.media = media
        val filter = FeedItemFilter(FeedItemFilter.HAS_MEDIA)
        assertTrue(filter.matches(item))
    }

    // ---- SubscriptionsFilter ----

    @Test
    fun subscriptionsFilter_emptyFilter_shouldBeDisabled() {
        val filter = SubscriptionsFilter("")
        assertFalse(filter.isEnabled())
    }

    @Test
    fun subscriptionsFilter_counterGreaterZero_shouldBeEnabled() {
        val filter = SubscriptionsFilter(SubscriptionsFilter.COUNTER_GREATER_ZERO)
        assertTrue(filter.isEnabled())
    }

    @Test
    fun subscriptionsFilter_autoDownloadEnabled_shouldBeEnabled() {
        val filter = SubscriptionsFilter(SubscriptionsFilter.ENABLED_AUTO_DOWNLOAD)
        assertTrue(filter.isEnabled())
    }

    @Test
    fun subscriptionsFilter_serialize_shouldProduceCommaSeparatedValues() {
        val filter = SubscriptionsFilter(
            SubscriptionsFilter.COUNTER_GREATER_ZERO + "," +
            SubscriptionsFilter.ENABLED_AUTO_DOWNLOAD
        )
        val values = filter.getValues()
        assertTrue(values.contains(SubscriptionsFilter.COUNTER_GREATER_ZERO))
        assertTrue(values.contains(SubscriptionsFilter.ENABLED_AUTO_DOWNLOAD))
    }

    // ---- FeedOrder ordinal consistency ----

    @Test
    fun feedOrder_ordinals_shouldMatchExpectedValues() {
        assertEquals(0, FeedOrder.COUNTER.ordinal)
        assertEquals(1, FeedOrder.ALPHABETICAL.ordinal)
        assertEquals(2, FeedOrder.MOST_PLAYED.ordinal)
        assertEquals(3, FeedOrder.MOST_RECENT_EPISODE.ordinal)
    }

    // ----

    private fun createTestFeed(id: Long): Feed {
        return Feed(
            id, "lm", "Feed $id", "https://ex.com", "Desc",
            "pay", "Author", "en", "rss", "fid$id",
            "https://img", "/f", "https://dl", 0L
        )
    }
}
