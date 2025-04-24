import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.SpojoyBaseballScrapePipeline
import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.logger
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

class SpojoyBaseballScrapePipelineTest {
    @Test
    fun `example 1`() = runBlocking {
        val browserPool = PlaywrightBrowserPool(3)

        val matchUrls: List<String> = SpojoyBaseballScrapePipeline.scrapeFixtureUrls(browserPool, League.Baseball.KBO)

        matchUrls
            .asFlow()
            .map { matchUrl ->
                val scrapedResult: Result<Pair<MatchInfo, LLMAttachment>> = SpojoyBaseballScrapePipeline.scrapeMatch(browserPool, League.Baseball.MLB, matchUrl)

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