import io.gitp.sbpick.pickgenerator.scraper.basketballscraper.FlashscoreBasketballScrapePipeline
import io.gitp.sbpick.pickgenerator.scraper.basketballscraper.models.BasketballMatchInfo
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking
import org.junit.Test

class FlashscoreBasketballScrapePipelineTest {
    @Test
    fun `example 1`() = runBlocking {
        val browserPool = PlaywrightBrowserPool(3)
        val scrapePipeline = FlashscoreBasketballScrapePipeline(browserPool)

        val matchInfoReceiver: ReceiveChannel<BasketballMatchInfo> = scrapePipeline.getFixtureUrl()
            .let { matchUrls -> with(scrapePipeline) { scrape(matchUrls) } }

        matchInfoReceiver
            .consumeEach { println(it) }
        browserPool.close()
    }
}