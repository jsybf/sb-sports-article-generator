package io.gitp.sbpick.pickgenerator.scraper.hockeyscraper

import io.gitp.sbpick.pickgenerator.scraper.hockeyscraper.extractors.*
import io.gitp.sbpick.pickgenerator.scraper.hockeyscraper.models.HockeyMatchInfo
import io.gitp.sbpick.pickgenerator.scraper.hockeyscraper.models.HockeyScraped
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.RequiredPageNotFound
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.LLMAttachment
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.MatchInfo
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.ScrapePipeline
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

object FlashscoreHockeyScrapePipeline : ScrapePipeline<League.Hockey> {

    override suspend fun scrapeFixtureUrls(browserPool: PlaywrightBrowserPool, league: League.Hockey): List<String> {
        logger.info("scraping flashscore-hockey-fixtures-page(league={}, url={})", league, league.matchListPageUrl)
        return browserPool.scrapeMatchListPage(league).extractMatchUrls()
    }

    override suspend fun scrapeMatch(browserPool: PlaywrightBrowserPool, league: League.Hockey, matchUrl: String): Result<Pair<MatchInfo, LLMAttachment>> = coroutineScope {
        logger.info("scraping hockey-match(match_page_url={})", matchUrl)
        runCatching {
            val matchPage = async { browserPool.scrapeMatchPage(matchUrl) }
            val oneXTwoBetOdds = async { browserPool.scrapeOneXTwoBetPage(matchUrl).extractOdds() }
            val overUnderBetOdds = async { browserPool.scrapeOverUnderBetPage(matchUrl).extractOdds() }

            if (oneXTwoBetOdds.await().size == 0 || oneXTwoBetOdds.await().size == 0) throw RequiredPageNotFound("odds section of ${matchUrl}")

            val (homeTeam, awayTeam) = matchPage.await().extractTeams()
            val matchInfo: HockeyMatchInfo = HockeyMatchInfo(
                awayTeam = awayTeam,
                homeTeam = homeTeam,
                matchAt = matchPage.await().extractMatchAt(),
                league = matchPage.await().extractLeague(),
                matchUniqueUrl = matchUrl
            )
            val scrapdResult: HockeyScraped = HockeyScraped(
                matchSummary = matchPage.await().parseMatchSummary(),
                oneXTwoBet = oneXTwoBetOdds.await(),
                overUnderBet = overUnderBetOdds.await(),
            )

            Pair(matchInfo, scrapdResult)
        }
    }
}