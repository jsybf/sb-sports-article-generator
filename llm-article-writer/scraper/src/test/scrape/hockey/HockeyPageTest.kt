package scrape.hockey

import org.jsoup.Jsoup
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals

class HockeyPageTest {
    private val matchSummaryPageSample = object {}
        .javaClass
        .getResource("/match.html")
        ?.readText()
        ?.let { Jsoup.parse(it) }
        ?: throw IllegalStateException("match.html doesn't exist in resource dir")

    @Test
    fun `parse home team and away team test`() {
        val sample = HockeyPage.SummaryPage(matchSummaryPageSample)
        assertEquals(Pair("일본 여", "노르웨이 여"), sample.parseTeam())
    }

    @Test
    fun `parse match start datetime test`() {
        val sample = HockeyPage.SummaryPage(matchSummaryPageSample)
        assertEquals(LocalDateTime.of(2025, 4, 10, 18, 0, 0), sample.parseStartDateTime())
    }
}
