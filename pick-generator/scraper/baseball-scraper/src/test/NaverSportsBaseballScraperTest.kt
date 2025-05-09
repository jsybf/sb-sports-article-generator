import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.NaverSportsBaseballScraper
import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.parseFixtures
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.time.LocalDate

class NaverSportsBaseballScraperTest {
    @Test
    fun `naver sports fixtures scrape just execute test`() {
        val sampleDate = LocalDate.of(2025, 5, 10)
        val browserPool = PlaywrightBrowserPool(3)
        runBlocking {
            (0L..3L)
                .flatMap { dayPlus -> listOf(League.Baseball.KBO, League.Baseball.MLB, League.Baseball.NPB).map { Pair(dayPlus, it) } }
                .map { (dayPlus, league) -> async { NaverSportsBaseballScraper.scrapeFixturePage(browserPool, league, sampleDate.plusDays(dayPlus)) } }
                .awaitAll()
                .flatMap { it.parseFixtures() }
                .onEach { println(it) }
        }
    }
}