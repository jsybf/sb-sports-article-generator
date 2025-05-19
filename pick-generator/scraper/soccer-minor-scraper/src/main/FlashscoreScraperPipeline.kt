package io.gitp.sbpick.pickgenerator.scraper.soccerminorscraper

import io.gitp.sbpick.pickgenerator.scraper.scrapebase.RequiredPageNotFound
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.LLMAttachment
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import io.gitp.sbpick.pickgenerator.scraper.soccerminorscraper.models.FlashscoreMinorSoccerMatchInfo
import io.gitp.sbpick.pickgenerator.scraper.soccerminorscraper.models.MinorSoccerScraped
import io.gitp.sbpick.pickgenerator.scraper.soccerminorscraper.pages.flashscore.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.opentest4j.AssertionFailedError
import java.time.LocalDate

object FlashscoreMinorSoccerScrapePipeline {
    suspend fun scrapeFixtures(browserPool: PlaywrightBrowserPool, league: League.MinorSoccer, matchAt: LocalDate): List<FlashscoreMinorSoccerMatchInfo> {
        logger.info("scraping flashscore soccer fixtures page(league=${league}, url=${league.fixtureListPageUrl})")
        return browserPool.scrapeFlashscoreFixturePage(league).getOrThrow().extractMatchInfo().filter { it.matchAt.toLocalDate() == matchAt }
    }

    suspend fun scrapeMatch(browserPool: PlaywrightBrowserPool, matchInfo: FlashscoreMinorSoccerMatchInfo): Result<LLMAttachment> = coroutineScope {
        logger.info("scraping flashscore soccer match (matchInfo={${matchInfo}})")
        runCatching {
            val matchPageDeferred = async { browserPool.scrapeFlashscoreMatchSummaryPage(matchInfo.flashscoreDetailPageUrl) }
            val oneXTwoBetOddsPageDeferred = async { browserPool.scrapeFlashscoreOneXTwoBetPage(matchInfo.flashscoreDetailPageUrl) }
            val overUnderBetOddsPageDeferred = async { browserPool.scrapeFlashscoreOverUnderBetPage(matchInfo.flashscoreDetailPageUrl) }

            val matchPage = matchPageDeferred.await().onFailure { e ->
                if (e is AssertionFailedError) throw RequiredPageNotFound("flashscore match summary page ${matchInfo}") else throw e
            }
            val oneXTwoBetOddsPage = oneXTwoBetOddsPageDeferred.await().onFailure { e ->
                if (e is AssertionFailedError) throw RequiredPageNotFound("flashscore match 1x2bet page ${matchInfo}") else throw e
            }
            val overUnderBetOddsPage = overUnderBetOddsPageDeferred.await().onFailure { e ->
                if (e is AssertionFailedError) throw RequiredPageNotFound("flashscore match over_under_bet page ${matchInfo}") else throw e
            }

            MinorSoccerScraped(
                matchSummary = matchPage.getOrThrow().extractMatchInfo(),
                oneXTwoBet = oneXTwoBetOddsPage.getOrThrow().extractOdds(),
                overUnderBet = overUnderBetOddsPage.getOrThrow().extractOdds()
            )
        }
    }
}
