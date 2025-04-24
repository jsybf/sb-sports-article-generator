package io.gitp.sbpick.pickgenerator.scraper.basketballscraper

import io.gitp.sbpick.pickgenerator.scraper.basketballscraper.extractors.*
import io.gitp.sbpick.pickgenerator.scraper.basketballscraper.models.BasketballMatchInfo
import io.gitp.sbpick.pickgenerator.scraper.basketballscraper.models.BasketballScraped
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.RequiredPageNotFound
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.LLMAttachment
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.MatchInfo
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.ScrapePipeline
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

object FlashscoreBasketballScrapePipeline : ScrapePipeline<League.Basketball> {
    override suspend fun scrapeFixtureUrls(browserPool: PlaywrightBrowserPool, league: League.Basketball): List<String> {
        logger.info("scraping flashscore-basketball-fixtures-page(league={}, url={})", league, league.matchListPageUrl)
        return browserPool.scrapeMatchListPage(league).extractMatchUrls()
    }

    override suspend fun scrapeMatch(browserPool: PlaywrightBrowserPool, league: League.Basketball, matchUrl: String): Result<Pair<MatchInfo, LLMAttachment>> = coroutineScope {
        logger.info("scraping basketball-match(match_page_url={})", matchUrl)
        runCatching {
            val matchPage = async { browserPool.scrapeMatchPage(matchUrl) }
            val oneXTwoBetOdds = async { browserPool.scrapeOneXTwoBetPage(matchUrl).extractOdds() }
            val overUnderBetOdds = async { browserPool.scrapeOverUnderBetPage(matchUrl).extactOdds() }

            if (oneXTwoBetOdds.await().size == 0 || oneXTwoBetOdds.await().size == 0) throw RequiredPageNotFound("odds section of ${matchUrl}")

            val (homeTeam, awayTeam) = matchPage.await().extractTeams()
            val matchInfo: BasketballMatchInfo = BasketballMatchInfo(
                awayTeam = awayTeam,
                homeTeam = homeTeam,
                matchAt = matchPage.await().extractMatchAt(),
                league = matchPage.await().extractLeague(),
                matchUniqueUrl = matchUrl
            )
            val scrapdResult: BasketballScraped = BasketballScraped(
                matchSummary = matchPage.await().extractMatchSummary(),
                oneXTwoBet = oneXTwoBetOdds.await(),
                overUnderBet = overUnderBetOdds.await(),
            )

            Pair(matchInfo, scrapdResult)
        }
    }
}