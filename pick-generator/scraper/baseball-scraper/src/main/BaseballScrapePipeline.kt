package io.gitp.sbpick.pickgenerator.scraper.baseballscraper

import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.extractors.*
import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.models.BaseballMatchInfo
import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.models.BaseballScraped
import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.models.NaverSportsBaseballMatchInfo
import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.models.SpojoyBaseballMatchInfo
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.RequiredPageNotFound
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.LLMAttachment
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import java.time.LocalDate

object BaseballScrapePipeline {
    suspend fun scrapeFixtures(browserPool: PlaywrightBrowserPool, league: League.Baseball, matchAt: LocalDate): List<BaseballMatchInfo> {
        logger.info("scraping spojoy-baseball-match-list-page(url=${league.matchListPageUrl(matchAt)})")
        val spojoyFixtureList: List<SpojoyBaseballMatchInfo> = browserPool.scrapeMatchListPage(league, matchAt).extractMlbMatchList()

        logger.info("scraping naver-sports-baseball-match-list-page(url=${league.matchListPageUrl(matchAt)})")
        val naverSportsFixtureList: List<NaverSportsBaseballMatchInfo> = NaverSportsBaseballScraper.scrapeFixturePage(browserPool, league, matchAt).extractFixtures()

        return spojoyFixtureList.mapNotNull { spojoyMatchInfo ->
            naverSportsFixtureList
                .find { naverSportsMatchInfo -> naverSportsMatchInfo.isEqual(spojoyMatchInfo) }
                ?.let { matchedNaverSportsFixture ->
                    BaseballMatchInfo(
                        awayTeam = spojoyMatchInfo.awayTeam,
                        homeTeam = spojoyMatchInfo.homeTeam,
                        matchAt = spojoyMatchInfo.matchAt,
                        league = league,
                        naverSportsDetailPageUrl = matchedNaverSportsFixture.matchDetailPageUrl,
                        spojoySportsDetailPageUrl = spojoyMatchInfo.matchDetailPageUrl
                    )
                }
        }
    }

    suspend fun scrapeMatch(browserPool: PlaywrightBrowserPool, matchInfo: BaseballMatchInfo): Result<LLMAttachment> = coroutineScope {
        logger.info("scraping baseball-match (league=${matchInfo.league},homeTeam=${matchInfo.homeTeam},awayTeam=${matchInfo.awayTeam},matchAt=${matchInfo.matchAt})")

        runCatching {
            val spojoyStartingPitchersPage = async { browserPool.scrapeStartingPitcherPage(matchInfo.spojoySportsDetailPageUrl) }
            val homeAwayPlayerListPagePair = async { browserPool.scrapePlayerListPage(matchInfo.spojoySportsDetailPageUrl) }
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

            if (!spojoyStartingPitchersPage.await().ifStartingPitcherUploaded()) throw RequiredPageNotFound("starting pitcher is not uploaded")

            var startingPitcherStatistics: JsonObject? = null
            var startingPitcherMostPitches: JsonObject? = null

            if (matchInfo.league == League.Baseball.KBO) {
                val naverSportsMatchInfo = NaverSportsBaseballScraper.scrapeMatchPage(browserPool, matchInfo.naverSportsDetailPageUrl)
                startingPitcherStatistics = naverSportsMatchInfo.extractStaringPitcherStatistics()
                startingPitcherMostPitches = naverSportsMatchInfo.extractStartingPitcherMostPitches()
            }


            BaseballScraped(
                startingPitcherInfo = spojoyStartingPitchersPage.await().extractPitcherStats(),
                batterInfo = buildJsonObject {
                    put(
                        "awayTeamBetter",
                        buildJsonArray { awayPlayerPages.await().map { it.extractPlayerInfo() }.forEach { add(it) } }
                    )
                    put(
                        "homeTeamBetter",
                        buildJsonArray { homePlayerPages.await().map { it.extractPlayerInfo() }.forEach { add(it) } }
                    )
                },
                startingPitcherStatistics = startingPitcherStatistics ?: buildJsonObject { },
                startingPitcherMostPitches = startingPitcherMostPitches ?: buildJsonObject { }
            )
        }
    }

}

suspend fun main() {
    val sampleDate = LocalDate.of(2025, 5, 11)
    val browserPool = PlaywrightBrowserPool(3)

    BaseballScrapePipeline.scrapeFixtures(browserPool, League.Baseball.NPB, sampleDate)
        .onEach { println(it) }
}