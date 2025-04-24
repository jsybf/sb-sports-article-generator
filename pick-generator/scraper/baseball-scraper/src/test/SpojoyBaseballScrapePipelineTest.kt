import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.SpojoyBaseballScrapePipeline
import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.models.BaseballMatchInfo
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.LLMAttachment
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking
import org.junit.Test

class SpojoyBaseballScrapePipelineTest {
    @Test
    fun `example 1`() = runBlocking {
        val browserPool = PlaywrightBrowserPool(4)
        val scrapePipeline = SpojoyBaseballScrapePipeline(browserPool)

        val matchInfoReceiver: ReceiveChannel<Pair<BaseballMatchInfo, LLMAttachment>> = scrapePipeline.getFixtureUrl(League.Baseball.KBO)
            .let { matchUrls -> with(scrapePipeline) { scrape(matchUrls) } }

        matchInfoReceiver
            .consumeEach { println(it) }
        browserPool.close()
    }
}