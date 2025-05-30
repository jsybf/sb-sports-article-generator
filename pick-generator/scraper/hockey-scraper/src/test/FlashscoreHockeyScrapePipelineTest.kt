import io.gitp.sbpick.pickgenerator.scraper.hockeyscraper.FlashscoreHockeyScrapePipeline
import io.gitp.sbpick.pickgenerator.scraper.hockeyscraper.logger
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.RequiredPageNotFound
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.LLMAttachment
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.time.LocalDate

class FlashscoreHockeyScrapePipelineTest {
    @Test
    fun `example 1`() = runBlocking {
        val browserPool = PlaywrightBrowserPool(3)

        val fixtures = FlashscoreHockeyScrapePipeline.scrapeFixtures(browserPool, League.Hockey.NHL, LocalDate.now())

        fixtures
            .asFlow()
            .map { matchInfo ->
                val scrapedResult: Result<LLMAttachment> = FlashscoreHockeyScrapePipeline.scrapeMatch(browserPool, matchInfo)

                scrapedResult.getOrElse { exception: Throwable ->
                    when (exception) {
                        is RequiredPageNotFound -> {
                            logger.warn(exception.message)
                            return@getOrElse null
                        }
                        else -> throw exception
                    }
                }?.let { Pair(matchInfo, it) }
            }
            .filterNotNull()
            .collect { (matchInfo, llmAttachment) -> println(matchInfo) }

        browserPool.close()
    }
}