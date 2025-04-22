package io.gitp.sbpick.pickgenerator.pickgenerator

import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.SpojoyBaseballScrapePipeline
import io.gitp.sbpick.pickgenerator.scraper.basketballscraper.FlashscoreBasketballScrapePipeline
import io.gitp.sbpick.pickgenerator.scraper.hockeyscraper.FlashscoreHockeyScrapePipeline
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League

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
}


