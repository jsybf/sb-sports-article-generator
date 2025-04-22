package io.gitp.sbpick.pickgenerator.pickgenerator

import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.SpojoyBaseballScrapePipeline
import io.gitp.sbpick.pickgenerator.scraper.basketballscraper.FlashscoreBasketballScrapePipeline
import io.gitp.sbpick.pickgenerator.scraper.hockeyscraper.FlashscoreHockeyScrapePipeline
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.MatchInfo
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import java.net.URI

class ScraperPipelineContainer(
    private val browserPool: PlaywrightBrowserPool
) {
    private val basketballScrapePipeline = FlashscoreBasketballScrapePipeline(browserPool)
    private val hockeyScrapePipeline = FlashscoreHockeyScrapePipeline(browserPool)
    private val baseballScrapePipeline = SpojoyBaseballScrapePipeline(browserPool)

    fun getScrapePipelineByLeague(league: League) = when (league) {
        is League.Basketball -> this.basketballScrapePipeline
        is League.Hockey -> this.hockeyScrapePipeline
        is League.Baseball -> this.baseballScrapePipeline
    }
}

class ScrapeService(
    private val browserPool: PlaywrightBrowserPool
) {
    private val scraperPipelineContainer = ScraperPipelineContainer(browserPool)

    suspend fun getMatchUrls(leagues: Set<League>): Map<League, List<URI>> = leagues
        .associateWith { league: League ->
            this.scraperPipelineContainer.getScrapePipelineByLeague(league).getFixtureUrl(league)
        }

    suspend fun scrapeAll(league: League, matchUrls: List<URI>): Flow<MatchInfo> = coroutineScope {
        val receiveChannel: ReceiveChannel<MatchInfo> = with(scraperPipelineContainer.getScrapePipelineByLeague(league)) { scrape(matchUrls) }
        val receiveAsFlow: Flow<MatchInfo> = receiveChannel.receiveAsFlow()
        receiveAsFlow
    }
}


