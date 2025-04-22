package io.gitp.sbpick.pickgenerator.scraper.baseballscraper

import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.extractors.*
import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.models.BaseballMatchInfo
import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.models.BaseballScraped
import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.models.StartingPitcerPage
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.LLMAttachment
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.ScrapePipeline
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

class SpojoyBaseballScrapePipeline(
    browserPool: PlaywrightBrowserPool
) : ScrapePipeline<BaseballMatchInfo, League.Baseball> {

    private val scraper = SpojoyBaseballScraper(browserPool)

    override suspend fun getFixtureUrl(league: League.Baseball): List<String> {
        logger.info("scraping spojoy-baseball-match-list-page(url=https://www.spojoy.com/live/?mct=baseball)")
        return scraper.scrapeMatchListPage().parseMlbMatchList()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun CoroutineScope.scrape(matchUrls: List<String>): ReceiveChannel<Pair<BaseballMatchInfo, LLMAttachment>> = produce {
        matchUrls.forEach { matchUrl ->
            logger.info("scraping spojoy-baseball-match (url=${matchUrl})")
            val startingPitchersPage: Deferred<StartingPitcerPage> = async { scraper.scrapeStartingPitcherPage(matchUrl) }
            val matchPage = async { scraper.scrapeMatchPage(matchUrl) }
            val homeAwayPlayerListPagePair = async { scraper.scrapePlayerListPage(matchUrl) }

            val homePlayerPages = async {
                homeAwayPlayerListPagePair
                    .await()
                    .first
                    .extractPlayerPageUrl()
                    .map { playerPageUrl -> async { scraper.scrapePlayerPage(playerPageUrl) } }
                    .awaitAll()
            }
            val awayPlayerPages = async {
                homeAwayPlayerListPagePair
                    .await()
                    .second
                    .extractPlayerPageUrl()
                    .map { playerPageUrl -> async { scraper.scrapePlayerPage(playerPageUrl) } }
                    .awaitAll()
            }

            val (homeTeamName, awayTeamName) = matchPage.await().extractTeamName()
            val baseballMatchInfo = BaseballMatchInfo(
                awayTeam = awayTeamName,
                homeTeam = homeTeamName,
                matchAt = matchPage.await().extractMatchAt(),
                league = matchPage.await().extractLeague(),
                matchUniqueUrl = matchUrl
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
            send(Pair(baseballMatchInfo, scrapedResult))
        }

    }

}