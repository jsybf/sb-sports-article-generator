import io.gitp.sbpick.pickgenerator.scraper.hockeyscraper.FlashscoreHockeyScrapePipeline
import io.gitp.sbpick.pickgenerator.scraper.hockeyscraper.models.HockeyMatchInfo
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking
import org.junit.Test

class FlashscoreHockeyScrapePipelineTest {
    @Test
    fun `example 1`() = runBlocking {
        val browserPool = PlaywrightBrowserPool(3)
        val scrapePipeline = FlashscoreHockeyScrapePipeline(browserPool)

        val matchInfoReceiver: ReceiveChannel<HockeyMatchInfo> = scrapePipeline.getFixtureUrl()
            .let { matchUrls -> with(scrapePipeline) { scrape(matchUrls) } }

        matchInfoReceiver
            .consumeEach { println(it) }
        browserPool.close()
    }
}