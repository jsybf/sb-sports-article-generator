package io.gitp.sbpick.pickgenerator.scraper.hockeyscraper

import io.gitp.sbpick.pickgenerator.scraper.hockeyscraper.extractors.extractMatchInfo
import io.gitp.sbpick.pickgenerator.scraper.hockeyscraper.extractors.extractOdds
import io.gitp.sbpick.pickgenerator.scraper.hockeyscraper.extractors.parseMatchSummary
import io.gitp.sbpick.pickgenerator.scraper.hockeyscraper.models.HockeyMatchInfo
import io.gitp.sbpick.pickgenerator.scraper.hockeyscraper.models.HockeyScraped
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.RequiredPageNotFound
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.LLMAttachment
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.LocalDate

object FlashscoreHockeyScrapePipeline {
    suspend fun scrapeFixtures(browserPool: PlaywrightBrowserPool, league: League.Hockey, matchAt: LocalDate): List<HockeyMatchInfo> {
        logger.info("scraping flashscore-hockey-fixtures-page(league={}, url={})", league, league.matchListPageUrl)
        return browserPool.scrapeMatchListPage(league).extractMatchInfo().filter { it.matchAt.toLocalDate() == matchAt }
    }

    suspend fun scrapeMatch(browserPool: PlaywrightBrowserPool, matchInfo: HockeyMatchInfo): Result<LLMAttachment> = coroutineScope {
        logger.info("scraping hockey-match(match_page_url={})", matchInfo.flashscoreDetailPageUrl)
        runCatching {
            val matchPage = async { browserPool.scrapeMatchPage(matchInfo.flashscoreDetailPageUrl) }
            val oneXTwoBetOdds = async { browserPool.scrapeOneXTwoBetPage(matchInfo.flashscoreDetailPageUrl).extractOdds() }
            val overUnderBetOdds = async { browserPool.scrapeOverUnderBetPage(matchInfo.flashscoreDetailPageUrl).extractOdds() }

            if (oneXTwoBetOdds.await().size == 0 || oneXTwoBetOdds.await().size == 0) throw RequiredPageNotFound("odds section of ${matchInfo.flashscoreDetailPageUrl}")

            HockeyScraped(
                matchSummary = matchPage.await().parseMatchSummary(),
                oneXTwoBet = oneXTwoBetOdds.await(),
                overUnderBet = overUnderBetOdds.await(),
            )
        }
    }
}