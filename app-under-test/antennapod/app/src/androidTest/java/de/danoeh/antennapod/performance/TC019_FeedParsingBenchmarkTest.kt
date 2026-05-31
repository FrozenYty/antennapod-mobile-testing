package de.danoeh.antennapod.performance

import android.content.ContentValues
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.danoeh.antennapod.storage.database.PodDBAdapter
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * TC-019: Feed Parsing Speed Benchmark
 *
 * Measures database insertion and query performance for feed-related
 * operations. Uses System.nanoTime() for manual timing since no
 * benchmark library (Macrobenchmark/Microbenchmark) is configured.
 *
 * Each test runs multiple iterations and verifies that the average
 * time per operation is within acceptable limits.
 *
 * Adaptation: No androidx.benchmark library available. Manual timing
 * with System.nanoTime() provides approximate measurements. Results
 * are informational — no strict pass/fail thresholds.
 *
 * @author Jianheng Sun
 */
@RunWith(AndroidJUnit4::class)
class TC019_FeedParsingBenchmarkTest {

    private lateinit var adapter: PodDBAdapter
    private lateinit var context: Context
    private val iterations = 20

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
    fun benchmark_feedInsert_shouldBeUnder100ms() {
        var totalNanos = 0L
        for (i in 0 until iterations) {
            val values = ContentValues().apply {
                put(PodDBAdapter.KEY_TITLE, "Benchmark Feed $i")
                put(PodDBAdapter.KEY_DOWNLOAD_URL, "https://bench$i.example/feed.xml")
                put(PodDBAdapter.KEY_LINK, "https://bench$i.example")
            }
            val start = System.nanoTime()
            adapter.insertTestData(PodDBAdapter.TABLE_NAME_FEEDS, values)
            totalNanos += System.nanoTime() - start
        }
        val avgMicros = totalNanos / iterations / 1000
        // Insert should typically be under 100ms (100,000 micros) per operation
        val avgMillis = avgMicros / 1000
        assertTrue(
            "Average feed insert time ${avgMillis}ms exceeds 100ms threshold",
            avgMillis < 100
        )
    }

    @Test
    fun benchmark_feedQuery_shouldBeUnder50ms() {
        // Insert test data first
        for (i in 0 until iterations) {
            val values = ContentValues().apply {
                put(PodDBAdapter.KEY_TITLE, "Query Feed $i")
                put(PodDBAdapter.KEY_DOWNLOAD_URL, "https://query$i.example/feed.xml")
                put(PodDBAdapter.KEY_LINK, "https://query$i.example")
            }
            adapter.insertTestData(PodDBAdapter.TABLE_NAME_FEEDS, values)
        }

        var totalNanos = 0L
        for (i in 0 until iterations) {
            val start = System.nanoTime()
            val cursor = adapter.allFeedsCursor
            while (cursor.moveToNext()) {
                // iterate all rows
            }
            cursor.close()
            totalNanos += System.nanoTime() - start
        }
        val avgMillis = totalNanos / iterations / 1_000_000
        assertTrue(
            "Average feed query time ${avgMillis}ms exceeds 50ms threshold",
            avgMillis < 50
        )
    }

    @Test
    fun benchmark_itemInsertWithFeed_shouldBeUnder200ms() {
        // Pre-insert a feed
        val feedId = insertBenchmarkFeed()
        var totalNanos = 0L
        for (i in 0 until iterations) {
            val values = ContentValues().apply {
                put(PodDBAdapter.KEY_TITLE, "Benchmark Episode $i")
                put(PodDBAdapter.KEY_FEED, feedId)
                put(PodDBAdapter.KEY_LINK, "https://bench$i.example/ep")
                put(PodDBAdapter.KEY_PUBDATE, System.currentTimeMillis())
            }
            val start = System.nanoTime()
            adapter.insertTestData(PodDBAdapter.TABLE_NAME_FEED_ITEMS, values)
            totalNanos += System.nanoTime() - start
        }
        val avgMillis = totalNanos / iterations / 1_000_000
        assertTrue(
            "Average item insert time ${avgMillis}ms exceeds 200ms threshold",
            avgMillis < 200
        )
    }

    private fun insertBenchmarkFeed(): Long {
        val values = ContentValues().apply {
            put(PodDBAdapter.KEY_TITLE, "Benchmark Parent Feed")
            put(PodDBAdapter.KEY_DOWNLOAD_URL, "https://bench-parent.example/feed.xml")
            put(PodDBAdapter.KEY_LINK, "https://bench-parent.example")
        }
        adapter.insertTestData(PodDBAdapter.TABLE_NAME_FEEDS, values)
        val cursor = adapter.allFeedsCursor
        var id = -1L
        while (cursor.moveToNext()) {
            val title = cursor.getString(cursor.getColumnIndexOrThrow(PodDBAdapter.KEY_TITLE))
            if (title == "Benchmark Parent Feed") {
                id = cursor.getLong(cursor.getColumnIndexOrThrow(PodDBAdapter.SELECT_KEY_FEED_ID))
                break
            }
        }
        cursor.close()
        return id
    }
}
