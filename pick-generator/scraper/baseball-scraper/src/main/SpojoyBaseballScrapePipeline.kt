package io.gitp.sbpick.pickgenerator.scraper.baseballscraper

import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.extractors.*
import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.models.BaseballScraped
import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.models.NaverSportsBaseballMatchInfo
import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.models.SpojoyBaseballMatchInfo
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.RequiredPageNotFound
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.LLMAttachment
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.MatchInfo
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import java.time.LocalDate

object SpojoyBaseballScrapePipeline {
    suspend fun scrapeFixtureUrls(browserPool: PlaywrightBrowserPool, league: League.Baseball, matchAt: LocalDate): List<String> {
        logger.info("scraping spojoy-baseball-match-list-page(url=${league.matchListPageUrl(matchAt)})")
        return browserPool.scrapeMatchListPage(league, matchAt).extractMlbMatchList().map { it.matchDetailPageUrl }
    }

    suspend fun scrapeFixtureUrl(browserPool: PlaywrightBrowserPool, league: League.Baseball, matchAt: LocalDate): List<SpojoyBaseballMatchInfo> {
        logger.info("scraping spojoy-baseball-match-list-page(url=${league.matchListPageUrl(matchAt)})")
        val spojoyFixtureList: List<SpojoyBaseballMatchInfo> =  browserPool.scrapeMatchListPage(league, matchAt).extractMlbMatchList()
        logger.info("scraping naver-sports-baseball-match-list-page(url=${league.matchListPageUrl(matchAt)})")
        val naverSportsFixtureList: List<NaverSportsBaseballMatchInfo> = NaverSportsBaseballScraper.scrapeFixturePage(browserPool, league, matchAt).extractFixtures()

    }

    suspend fun scrapeMatch(browserPool: PlaywrightBrowserPool, league: League.Baseball, matchUrl: String): Result<Pair<MatchInfo, LLMAttachment>> = coroutineScope {
        logger.info("scraping spojoy-baseball-match (url=${matchUrl})")
        runCatching {
            val startingPitchersPage = async { browserPool.scrapeStartingPitcherPage(matchUrl) }
            val matchPage = async { browserPool.scrapeMatchPage(matchUrl) }
            val homeAwayPlayerListPagePair = async { browserPool.scrapePlayerListPage(matchUrl) }
            val homePlayerPages = async {
                homeAwayPlayerListPagePair
                    .await()
                    .first
                    .extractPlayerPageUrl()
                    .map { playerPageUrl -> async { browserPool.scrapePlayerPage(playerPageUrl) } }
                    .awaitAll()
            }
            val awayPlayerPages = async {
                homeAwayPlayerListPagePair
                    .await()
                    .second
                    .extractPlayerPageUrl()
                    .map { playerPageUrl -> async { browserPool.scrapePlayerPage(playerPageUrl) } }
                    .awaitAll()
            }
            if (!startingPitchersPage.await().ifStartingPitcherUploaded()) throw RequiredPageNotFound("starting pitcher is not uploaded")
            val (homeTeamName, awayTeamName) = matchPage.await().extractTeamName()
            val baseballMatchInfo = SpojoyBaseballMatchInfo(
                awayTeam = awayTeamName,
                homeTeam = homeTeamName,
                matchAt = matchPage.await().extractMatchAt(),
                league = matchPage.await().extractLeague(),
                matchDetailPageUrl = matchUrl
            )
            val scrapedResult = BaseballScraped(
                startingPitcherInfo = startingPitchersPage.await().extractPitcherStats(),
                batterInfo = buildJsonObject {
                    put(
                        "awayTeamBetter",
                        buildJsonArray { awayPlayerPages.await().map { it.extractPlayerInfo() }.forEach { add(it) } }
                    )
                    put(
                        "homeTeamBetter",
                        buildJsonArray { homePlayerPages.await().map { it.extractPlayerInfo() }.forEach { add(it) } }
                    )
                }
            )
            Pair(baseballMatchInfo, scrapedResult)
        }
    }

}

suspend fun main() {
    val sampleDate = LocalDate.of(2025, 5, 11)
    val browserPool = PlaywrightBrowserPool(3)

    SpojoyBaseballScrapePipeline.scrapeFixtureUrl(browserPool, League.Baseball.NPB, sampleDate)
        .onEach { println(it) }
}