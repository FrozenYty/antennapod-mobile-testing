package de.danoeh.antennapod.unit

import de.danoeh.antennapod.net.common.UrlChecker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * TC-016: Feed URL Parsing & Normalization
 *
 * Validates UrlChecker.prepareUrl() behavior for various URL formats
 * encountered during podcast subscription. Covers protocol conversion,
 * deeplink extraction, whitespace handling, and edge cases.
 *
 * Uses RobolectricTestRunner to mock Android Log calls within UrlChecker.
 *
 * @author Jianheng Sun
 */
@RunWith(RobolectricTestRunner::class)
class TC016_FeedUrlParsingTest {

    // ---- Valid URLs (no change expected) ----

    @Test
    fun prepareUrl_validHttpUrl_shouldReturnUnchanged() {
        val result = UrlChecker.prepareUrl("http://example.com/feed.xml")
        assertEquals("http://example.com/feed.xml", result)
    }

    @Test
    fun prepareUrl_validHttpsUrl_shouldReturnUnchanged() {
        val result = UrlChecker.prepareUrl("https://example.com/podcast.rss")
        assertEquals("https://example.com/podcast.rss", result)
    }

    // ---- Missing protocol ----

    @Test
    fun prepareUrl_missingProtocol_shouldAddHttp() {
        val result = UrlChecker.prepareUrl("example.com/feed.xml")
        assertEquals("http://example.com/feed.xml", result)
    }

    @Test
    fun prepareUrl_wwwWithoutProtocol_shouldAddHttp() {
        val result = UrlChecker.prepareUrl("www.example.com/podcast")
        assertEquals("http://www.example.com/podcast", result)
    }

    // ---- Protocol conversion ----

    @Test
    fun prepareUrl_feedProtocol_shouldReplaceWithHttp() {
        val result = UrlChecker.prepareUrl("feed://example.com/podcast.xml")
        assertEquals("http://example.com/podcast.xml", result)
    }

    @Test
    fun prepareUrl_feedProtocolWithHttpsPath_shouldKeepHttps() {
        val result = UrlChecker.prepareUrl("feed://https://example.com/feed")
        assertEquals("https://example.com/feed", result)
    }

    @Test
    fun prepareUrl_itpcProtocol_shouldReplaceWithHttp() {
        val result = UrlChecker.prepareUrl("itpc://example.com/podcast")
        assertEquals("http://example.com/podcast", result)
    }

    @Test
    fun prepareUrl_itpcProtocolWithHttpsPath_shouldKeepHttps() {
        val result = UrlChecker.prepareUrl("itpc://https://example.com/feed.xml")
        assertEquals("https://example.com/feed.xml", result)
    }

    @Test
    fun prepareUrl_pcastWithDoubleSlash_shouldRemoveProtocol() {
        val result = UrlChecker.prepareUrl("pcast://example.com/podcast")
        assertEquals("http://example.com/podcast", result)
    }

    @Test
    fun prepareUrl_pcastWithColonOnly_shouldRemoveProtocol() {
        val result = UrlChecker.prepareUrl("pcast:example.com/podcast")
        assertEquals("http://example.com/podcast", result)
    }

    // ---- AntennaPod subscribe protocol ----

    @Test
    fun prepareUrl_antennapodSubscribeProtocol_shouldRemoveProtocol() {
        val result = UrlChecker.prepareUrl("antennapod-subscribe://example.com/feed")
        assertEquals("http://example.com/feed", result)
    }

    @Test
    fun prepareUrl_antennapodSubscribeWithHttps_shouldKeepHttps() {
        val result = UrlChecker.prepareUrl("antennapod-subscribe://https://example.com/feed")
        assertEquals("https://example.com/feed", result)
    }

    // ---- Deeplink extraction ----

    @Test
    fun prepareUrl_antennapodDeeplink_shouldExtractUrlParam() {
        val result = UrlChecker.prepareUrl(
            "https://antennapod.org/deeplink/subscribe?url=http://example.com/feed.xml"
        )
        assertEquals("http://example.com/feed.xml", result)
    }

    @Test
    fun prepareUrl_antennapodDeeplinkHttps_shouldExtractUrlParam() {
        val result = UrlChecker.prepareUrl(
            "https://antennapod.org/deeplink/subscribe?url=https://example.com/feed.rss"
        )
        assertEquals("https://example.com/feed.rss", result)
    }

    // ---- Whitespace trimming ----

    @Test
    fun prepareUrl_leadingWhitespace_shouldTrim() {
        val result = UrlChecker.prepareUrl("  https://example.com/feed.xml")
        assertEquals("https://example.com/feed.xml", result)
    }

    @Test
    fun prepareUrl_trailingWhitespace_shouldTrim() {
        val result = UrlChecker.prepareUrl("https://example.com/feed.xml  ")
        assertEquals("https://example.com/feed.xml", result)
    }

    @Test
    fun prepareUrl_newlineAndTab_shouldTrim() {
        val result = UrlChecker.prepareUrl("\nhttp://example.com/feed\t")
        assertEquals("http://example.com/feed", result)
    }

    // ---- SubscribeOnAndroid removal ----

    @Test
    fun prepareUrl_subscribeOnAndroid_shouldRemovePrefix() {
        val result = UrlChecker.prepareUrl(
            "http://subscribeonandroid.com/example.com/feed.xml"
        )
        assertEquals("http://example.com/feed.xml", result)
    }

    // ---- Case insensitivity ----

    @Test
    fun prepareUrl_uppercaseFeedProtocol_shouldNormalize() {
        val result = UrlChecker.prepareUrl("FEED://example.com/podcast.xml")
        assertEquals("http://example.com/podcast.xml", result)
    }

    @Test
    fun prepareUrl_mixedCaseItpc_shouldNormalize() {
        val result = UrlChecker.prepareUrl("ITPC://example.com/podcast")
        assertEquals("http://example.com/podcast", result)
    }

    // ---- urlEquals ----

    @Test
    fun urlEquals_sameUrls_shouldBeEqual() {
        assertTrue(UrlChecker.urlEquals(
            "https://example.com/podcast",
            "https://example.com/podcast"
        ))
    }

    @Test
    fun urlEquals_differentHosts_shouldNotBeEqual() {
        assertFalse(UrlChecker.urlEquals(
            "https://example1.com/podcast",
            "https://example2.com/podcast"
        ))
    }

    @Test
    fun urlEquals_trailingSlashDifference_shouldBeEqual() {
        assertTrue(UrlChecker.urlEquals(
            "https://example.com/podcast",
            "https://example.com/podcast/"
        ))
    }

    @Test
    fun urlEquals_caseInsensitiveHost_shouldBeEqual() {
        assertTrue(UrlChecker.urlEquals(
            "https://Example.COM/podcast",
            "https://example.com/podcast"
        ))
    }
}
