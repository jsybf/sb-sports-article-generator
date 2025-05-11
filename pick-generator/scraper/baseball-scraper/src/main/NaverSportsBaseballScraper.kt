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

private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
internal fun NaverSportsBaseballMatchListPage.parseFixtures(): List<NaverSportsBaseballMatchInfo> {
    return this.doc
        .select(".ScheduleAllType_match_list_group__1nFDy")
        .firstOrNull { element ->
            element.selectFirst(".ScheduleAllType_title___Qfd4")!!.text() == when (this.league) {
                League.Baseball.KBO -> "KBO리그"
                League.Baseball.MLB -> "MLB"
                League.Baseball.NPB -> "NPB"
            }
        }
        ?.select("a.MatchBox_link_match_end__3HGjy")
        ?.map { aElement: Element ->
            val matchUri = aElement.attribute("href")!!.value  // example: "/game/20250509HIYO0"
            val findByAnyCode = when (this.league) {
                League.Baseball.KBO -> BaseballTeam.KBOTeam.Companion::findByAnyCode
                League.Baseball.MLB -> BaseballTeam.MLBTeam.Companion::findByAnyCode
                League.Baseball.NPB -> BaseballTeam.NPBTeam.Companion::findByAnyCode
            }
            NaverSportsBaseballMatchInfo(
                homeTeam = findByAnyCode(matchUri.slice(14..15)).enumName(),
                awayTeam = findByAnyCode(matchUri.slice(16..17)).enumName(),
                matchAt = LocalDateTime.of(LocalDate.parse(matchUri.slice(6..13), dateFormatter), LocalTime.of(0, 0, 0)),
                league = this.league,
                matchDetailPageUrl = "https://m.sports.naver.com${matchUri}"
            )
        }
        ?: emptyList()

}