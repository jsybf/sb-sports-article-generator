package io.gitp.sbpick.pickgenerator.scraper.basketballscraper

import com.microsoft.playwright.PlaywrightException
import io.gitp.sbpick.pickgenerator.scraper.basketballscraper.extractors.*
import io.gitp.sbpick.pickgenerator.scraper.basketballscraper.models.BasketballMatchInfo
import io.gitp.sbpick.pickgenerator.scraper.basketballscraper.models.BasketballMatchPage
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.ScrapePipeline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import java.net.URI

class FlashscoreBasketballScrapePipeline(
    browserPool: PlaywrightBrowserPool
) : ScrapePipeline<BasketballMatchInfo, League.Basketball> {
    private val scraper = FlashscoreBasketballScraper(browserPool)
    override suspend fun getAllFixtureUrls(): List<URI> = League.Basketball.entries.flatMap { league ->
        logger.info("scraping flashscore-hockey-match-list-page(url={})", league.matchListPageUrl)
        scraper.scrapeMatchListPage(league).extractMatchUrls()
    }

    override suspend fun getFixtureUrl(league: League.Basketball): List<URI> {
        logger.info("scraping flashscore-hockey-match-list-page(url={})", league.matchListPageUrl)
        return scraper.scrapeMatchListPage(league).extractMatchUrls()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun CoroutineScope.scrape(matchUrls: List<URI>): ReceiveChannel<BasketballMatchInfo> = produce {
        for (matchUrl in matchUrls) {
            // 스크래핑시 배당률 관련 버튼들이 아직 존재하지 않으면 playwright가 오류를 뱉음. 이오류는 넘겨야함.
            val matchInfo: BasketballMatchInfo? = runCatching {
                logger.info("scraping flashscore-hockey-match-page(url={})", matchUrl)
                val matchPage: Deferred<BasketballMatchPage> = async { scraper.scrapeMatchPage(matchUrl) }
                val oneXTwoBetOdds = async { scraper.scrapeOneXTwoBetPage(matchUrl).extractOdds() }
                val overUnderBetOdds = async { scraper.scrapeOverUnderBetPage(matchUrl).extactOdds() }

                if (overUnderBetOdds.await().size == 0 || oneXTwoBetOdds.await().size == 0) throw NoSuchElementException("odds doesn't exists")

                val (homeTeam, awayTeam) = matchPage.await().extractTeams()
                BasketballMatchInfo(
                    awayTeam = awayTeam,
                    homeTeam = homeTeam,
                    matchAt = matchPage.await().extractMatchAt(),
                    league = matchPage.await().extractLeague(),
                    matchSummary = matchPage.await().extractMatchSummary(),
                    oneXTwoBet = oneXTwoBetOdds.await(),
                    overUnderBet = overUnderBetOdds.await(),
                )
            }.onFailure { exception: Throwable -> if (exception !is PlaywrightException && exception !is NoSuchElementException) throw exception }.getOrNull()

            matchInfo?.let { send(it) }
        }
    }
}