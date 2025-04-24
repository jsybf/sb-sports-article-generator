import io.gitp.sbpick.pickgenerator.scraper.basketballscraper.FlashscoreBasketballScrapePipeline
import io.gitp.sbpick.pickgenerator.scraper.basketballscraper.logger
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.RequiredPageNotFound
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.LLMAttachment
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.MatchInfo
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Test

class FlashscoreBasketballScrapePipelineTest {
    @Test
    fun `example 1`() = runBlocking {
        val browserPool = PlaywrightBrowserPool(3)

        val matchUrls: List<String> = FlashscoreBasketballScrapePipeline.scrapeFixtureUrls(browserPool, League.Basketball.CBA)

        matchUrls
            .asFlow()
            .map { matchUrl ->
                val scrapedResult: Result<Pair<MatchInfo, LLMAttachment>> = FlashscoreBasketballScrapePipeline.scrapeMatch(browserPool, League.Basketball.CBA, matchUrl)

                scrapedResult.getOrElse { exception: Throwable ->
                    when (exception) {
                        is RequiredPageNotFound -> {
                            logger.warn(exception.message)
                            return@map null
                        }
                        else -> throw exception
                    }
                }
            }
            .filterNotNull()
            .collect { (matchInfo, llmAttachment) -> println(matchInfo) }

        browserPool.close()
    }
}