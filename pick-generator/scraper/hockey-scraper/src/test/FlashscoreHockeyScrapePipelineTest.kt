import io.gitp.sbpick.pickgenerator.scraper.hockeyscraper.FlashscoreHockeyScrapePipeline
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking
import org.junit.Test

class FlashscoreHockeyScrapePipelineTest {
    @Test
    fun `example 1`() = runBlocking {
        val browserPool = PlaywrightBrowserPool(3)
        val scrapePipeline = FlashscoreHockeyScrapePipeline(browserPool)

        val matchInfoReceiver = scrapePipeline.getFixtureUrl(League.Hockey.NHL)
            .let { matchUrls -> with(scrapePipeline) { scrape(matchUrls) } }

        matchInfoReceiver
            .consumeEach { println(it) }
        browserPool.close()
    }
}