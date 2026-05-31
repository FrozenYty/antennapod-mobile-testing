package de.danoeh.antennapod.integration

import android.content.ContentValues
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.danoeh.antennapod.model.feed.Feed
import de.danoeh.antennapod.storage.database.DBReader
import de.danoeh.antennapod.storage.database.PodDBAdapter
import de.danoeh.antennapod.storage.importexport.OpmlElement
import de.danoeh.antennapod.storage.importexport.OpmlReader
import de.danoeh.antennapod.storage.importexport.OpmlWriter
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.StringReader
import java.io.StringWriter

/**
 * TC-037: Data Export & Import Integrity
 *
 * Verifies OPML export/import round-trip integrity using feed records read
 * from PodDBAdapter through DBReader.
 *
 * @author Member Four
 */
@RunWith(AndroidJUnit4::class)
class TC037_DataExportImportIntegrityTest {

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
    fun opmlRoundTrip_subscribedFeed_shouldPreserveCoreFields() {
        insertFeed(
            title = "Integrity Feed",
            downloadUrl = "https://integrity.example/feed.xml",
            link = "https://integrity.example",
            type = "rss",
            state = Feed.STATE_SUBSCRIBED
        )

        val elements = exportAndReadElements()
        val exported = elements.first { it.xmlUrl == "https://integrity.example/feed.xml" }

        assertEquals("Integrity Feed", exported.text)
        assertEquals("https://integrity.example", exported.htmlUrl)
        assertEquals("rss", exported.type)
    }

    @Test
    fun opmlExport_shouldSkipUnsubscribedAndArchivedFeeds() {
        insertFeed("Subscribed Export", "https://export.example/sub.xml", state = Feed.STATE_SUBSCRIBED)
        insertFeed("Not Subscribed Export", "https://export.example/not.xml", state = Feed.STATE_NOT_SUBSCRIBED)
        insertFeed("Archived Export", "https://export.example/archive.xml", state = Feed.STATE_ARCHIVED)

        val exportedUrls = exportAndReadElements().map { it.xmlUrl }

        assertTrue(exportedUrls.contains("https://export.example/sub.xml"))
        assertFalse(exportedUrls.contains("https://export.example/not.xml"))
        assertFalse(exportedUrls.contains("https://export.example/archive.xml"))
    }

    @Test
    fun dbReaderDownloadUrls_subscribedOnly_shouldExcludeArchivedAndLocalFeeds() {
        insertFeed("Remote Subscribed", "https://remote.example/feed.xml", state = Feed.STATE_SUBSCRIBED)
        insertFeed("Remote Archived", "https://remote.example/archive.xml", state = Feed.STATE_ARCHIVED)
        insertFeed("Local Folder", Feed.PREFIX_LOCAL_FOLDER + "storage/podcasts", state = Feed.STATE_SUBSCRIBED)

        val urls = DBReader.getFeedListDownloadUrls(true)

        assertTrue(urls.contains("https://remote.example/feed.xml"))
        assertFalse(urls.contains("https://remote.example/archive.xml"))
        assertFalse(urls.any { it.startsWith(Feed.PREFIX_LOCAL_FOLDER) })
    }

    @Test
    fun opmlRoundTrip_multipleFeeds_shouldKeepAllSubscribedUrls() {
        insertFeed("Alpha Export", "https://multi.example/alpha.xml", state = Feed.STATE_SUBSCRIBED)
        insertFeed("Beta Export", "https://multi.example/beta.xml", state = Feed.STATE_SUBSCRIBED)
        insertFeed("Gamma Export", "https://multi.example/gamma.xml", state = Feed.STATE_SUBSCRIBED)

        val exportedUrls = exportAndReadElements().map { it.xmlUrl }.toSet()

        assertTrue(exportedUrls.contains("https://multi.example/alpha.xml"))
        assertTrue(exportedUrls.contains("https://multi.example/beta.xml"))
        assertTrue(exportedUrls.contains("https://multi.example/gamma.xml"))
    }

    private fun exportAndReadElements(): List<OpmlElement> {
        val writer = StringWriter()
        OpmlWriter.writeDocument(DBReader.getFeedList(), writer)
        return OpmlReader().readDocument(StringReader(writer.toString()))
    }

    private fun insertFeed(
        title: String,
        downloadUrl: String,
        link: String = "https://example.com",
        type: String = "rss",
        state: Int
    ) {
        val values = ContentValues().apply {
            put(PodDBAdapter.KEY_TITLE, title)
            put(PodDBAdapter.KEY_DOWNLOAD_URL, downloadUrl)
            put(PodDBAdapter.KEY_LINK, link)
            put(PodDBAdapter.KEY_TYPE, type)
            put(PodDBAdapter.KEY_STATE, state)
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_FEEDS, values)
    }
}
