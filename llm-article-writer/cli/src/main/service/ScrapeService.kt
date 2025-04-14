package io.gitp.llmarticlewriter.cli.service

import io.gitp.llmarticlewriter.cli.toHockeyMatchDto
import io.gitp.llmarticlewriter.cli.toHockeyScrapedPageDto
import io.gitp.llmarticlewriter.database.SportsRepository
import io.gitp.llmarticlewriter.scraper.PlaywrightBrowser
import io.gitp.llmarticlewriter.scraper.model.League
import io.gitp.llmarticlewriter.scraper.model.MatchInfo
import io.gitp.llmarticlewriter.scraper.service.BasketBallScrapeSrevice
import io.gitp.llmarticlewriter.scraper.service.HockeyScrapeService

internal class ScrapeService(
    private val repo: SportsRepository
) {

    fun scrapeLeague(league: League) = PlaywrightBrowser().use { browser ->
        val matchList: Sequence<MatchInfo> = when (league) {
            is League.Hockey -> HockeyScrapeService(browser).scrapeUpcommingMatch(league)
            is League.BasketBall -> BasketBallScrapeSrevice(browser).scrapeUpcommingMatch(league)
        }

        matchList
            .forEach { matchInfo ->
                val matchId = repo.insertHockeyMatch(matchInfo.toHockeyMatchDto())
                repo.insertHockeyScrapedPage(matchId, matchInfo.toHockeyScrapedPageDto())
            }
    }
}