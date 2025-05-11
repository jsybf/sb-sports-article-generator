package io.gitp.sbpick.pickgenerator.scraper.basketballscraper

import io.gitp.sbpick.pickgenerator.scraper.basketballscraper.extractors.extractMatchInfo
import io.gitp.sbpick.pickgenerator.scraper.basketballscraper.extractors.extractMatchSummary
import io.gitp.sbpick.pickgenerator.scraper.basketballscraper.extractors.extractOdds
import io.gitp.sbpick.pickgenerator.scraper.basketballscraper.models.BasketballMatchInfo
import io.gitp.sbpick.pickgenerator.scraper.basketballscraper.models.BasketballScraped
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.RequiredPageNotFound
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.LLMAttachment
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.LocalDate

object FlashscoreBasketballScrapePipeline {
    suspend fun scrapeFixtures(browserPool: PlaywrightBrowserPool, league: League.Basketball, matchAt: LocalDate): List<BasketballMatchInfo> {
        logger.info("scraping flashscore-basketball-fixtures-page(league={}, url={})", league, league.matchListPageUrl)
        return browserPool.scrapeMatchListPage(league).extractMatchInfo().filter { it.matchAt.toLocalDate() == matchAt }
    }

    suspend fun scrapeMatch(browserPool: PlaywrightBrowserPool, matchInfo: BasketballMatchInfo): Result<LLMAttachment> = coroutineScope {
        logger.info("scraping basketball-match(match_page_url={})", matchInfo.flashscoreDetailPageUrl)
        runCatching {
            val matchPage = async { browserPool.scrapeMatchPage(matchInfo.flashscoreDetailPageUrl) }
            val oneXTwoBetOdds = async { browserPool.scrapeOneXTwoBetPage(matchInfo.flashscoreDetailPageUrl).extractOdds() }
            val overUnderBetOdds = async { browserPool.scrapeOverUnderBetPage(matchInfo.flashscoreDetailPageUrl).extractOdds() }

            if (oneXTwoBetOdds.await().size == 0 || oneXTwoBetOdds.await().size == 0) throw RequiredPageNotFound("odds section of ${matchInfo.flashscoreDetailPageUrl}")

            BasketballScraped(
                matchSummary = matchPage.await().extractMatchSummary(),
                oneXTwoBet = oneXTwoBetOdds.await(),
                overUnderBet = overUnderBetOdds.await(),
            )
        }
    }
}