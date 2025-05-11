import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.BaseballScrapePipeline
import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.logger
import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.models.BaseballMatchInfo
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
import java.time.LocalDate

class BaseballScrapePipelineTest {
    @Test
    fun `example 1`() = runBlocking {
        val browserPool = PlaywrightBrowserPool(3)

        val matchUrls: List<MatchInfo> = BaseballScrapePipeline.scrapeFixtures(browserPool, League.Baseball.KBO, LocalDate.now().plusDays(1))

        matchUrls
            .asFlow()
            .map { matchInfo ->
                val scrapedResult: Result<LLMAttachment> = BaseballScrapePipeline.scrapeMatch(browserPool, matchInfo as BaseballMatchInfo)

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
            .collect { matchInfo -> println(matchInfo) }

        browserPool.close()

    }
}