package extractors

import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.extractors.extractStaringPitcherStatistics
import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.extractors.extractStartingPitcherMostPitches
import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.models.NaverSportsBaseballMatchPage
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Test

class NaverSportsMatchPageExtractorTest {
    @Test
    fun `test extract pitcher's most pitch by just running it`() {
        runBlocking {
            val browserPool = PlaywrightBrowserPool(1)
            val matchPage = browserPool
                .doAndGetDoc { navigate("https://m.sports.naver.com/game/20250510LTKT12025") }
                .let { NaverSportsBaseballMatchPage(it) }


            matchPage.extractStaringPitcherStatistics().also { println(Json { prettyPrint = true }.encodeToString(it)) }
            matchPage.extractStartingPitcherMostPitches().also { println(Json { prettyPrint = true }.encodeToString(it)) }
        }
    }
}