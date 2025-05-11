package io.gitp.sbpick.pickgenerator.scraper.baseballscraper

import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.models.NaverSportsBaseballMatchInfo
import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.models.NaverSportsBaseballMatchListPage
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.BaseballTeam
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

internal object NaverSportsBaseballScraper {
    suspend fun scrapeFixturePage(browserPool: PlaywrightBrowserPool, league: League.Baseball, date: LocalDate): NaverSportsBaseballMatchListPage {
        val fixturePageUrl = when (league) {
            League.Baseball.MLB -> "https://m.sports.naver.com/wbaseball/schedule/index?date=${date}"
            League.Baseball.NPB -> "https://m.sports.naver.com/wbaseball/schedule/index?date=${date}"
            League.Baseball.KBO -> "https://m.sports.naver.com/kbaseball/schedule/index?date=${date}"
        }

        return browserPool
            .doAndGetDocAsync {
                logger.debug("scraping naver-sports-${league}-fixture-page (url=${fixturePageUrl})")
                this.navigate(fixturePageUrl)
            }
            .await()
            .let { NaverSportsBaseballMatchListPage(it, league) }
    }
}
