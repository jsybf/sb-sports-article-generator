package io.gitp.sbpick.pickgenerator.scraper.hockeyscraper

import com.microsoft.playwright.PlaywrightException
import io.gitp.sbpick.pickgenerator.scraper.hockeyscraper.extractors.*
import io.gitp.sbpick.pickgenerator.scraper.hockeyscraper.models.HockeyMatchInfo
import io.gitp.sbpick.pickgenerator.scraper.hockeyscraper.models.HockeyMatchPage
import io.gitp.sbpick.pickgenerator.scraper.hockeyscraper.models.HockeyScraped
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.LLMAttachment
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.ScrapePipeline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import java.net.URI

class FlashscoreHockeyScrapePipeline(
    browserPool: PlaywrightBrowserPool
) : ScrapePipeline<HockeyMatchInfo, League.Hockey> {
    private val scraper = FlashscoreHockeyScraper(browserPool)

    override suspend fun getFixtureUrl(league: League.Hockey): List<URI> {
        logger.info("scraping flashscore-hockey-match-list-page(url={})", league.matchListPageUrl)
        return scraper.scrapeMatchListPage(league).extractMatchUrls()
    }

    override fun CoroutineScope.scrape(matchUrls: List<URI>): ReceiveChannel<Pair<HockeyMatchInfo, LLMAttachment>> = produce {
        for (matchUrl in matchUrls) {
            // 스크래핑시 배당률 관련 버튼들이 아직 존재하지 않으면 playwright가 오류를 뱉음. 이오류는 넘겨야함.
            runCatching {
                logger.info("scraping flashscore-hockey-match-page(url={})", matchUrl)
                val matchPage: Deferred<HockeyMatchPage> = async { scraper.scrapeMatchPage(matchUrl) }
                val oneXTwoBetOdds = async { scraper.scrapeOneXTwoBetPage(matchUrl).extractOdds() }
                val overUnderBetOdds = async { scraper.scrapeOverUnderBetPage(matchUrl).extactOdds() }

                if (overUnderBetOdds.await().size == 0 || oneXTwoBetOdds.await().size == 0) throw NoSuchElementException("odds doesn't exists")

                val (homeTeam, awayTeam) = matchPage.await().extractTeams()
                val matchInfo = HockeyMatchInfo(
                    awayTeam = awayTeam,
                    homeTeam = homeTeam,
                    matchAt = matchPage.await().extractMatchAt(),
                    league = matchPage.await().extractLeague(),
                )
                val scrapdResult = HockeyScraped(
                    matchSummary = matchPage.await().parseMatchSummary(),
                    oneXTwoBet = oneXTwoBetOdds.await(),
                    overUnderBet = overUnderBetOdds.await(),
                )
                send(Pair(matchInfo, scrapdResult))
            }.onFailure { exception: Throwable -> if (exception !is PlaywrightException && exception !is NoSuchElementException) throw exception }
        }
    }
}