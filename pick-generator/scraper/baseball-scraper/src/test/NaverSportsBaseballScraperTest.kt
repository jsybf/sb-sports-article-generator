import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.BaseballScrapePipeline
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.time.LocalDate

class NaverSportsBaseballScraperTest {
    @Test
    fun `naver sports fixtures scrape just execute test`() {
        val browserPool = PlaywrightBrowserPool(1)

        runBlocking {
            BaseballScrapePipeline
                .scrapeFixtures(browserPool, League.Baseball.MLB, LocalDate.of(2025, 5, 14)) // date is hard codeded
                .forEach { println(it) }
        }
    }
}