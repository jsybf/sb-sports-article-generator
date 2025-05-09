package io.gitp.sbpick.pickgenerator.scraper.baseballscraper

import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.models.NaverSportsMatchListPage
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.BaseballTeam
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class NaverSportsMatch(
    val homeTeam: BaseballTeam,
    val awayTeam: BaseballTeam,
    val matchDate: LocalDate,
    val url: String
)

internal object NaverSportsBaseballScraper {
    suspend fun scrapeFixturePage(browserPool: PlaywrightBrowserPool, league: League.Baseball, date: LocalDate): NaverSportsMatchListPage {
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
            .let { NaverSportsMatchListPage(it, league) }
    }
}

private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
internal fun NaverSportsMatchListPage.parseFixtures(): List<NaverSportsMatch> {
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
            NaverSportsMatch(
                homeTeam = findByAnyCode(matchUri.slice(14..15)),
                awayTeam = findByAnyCode(matchUri.slice(16..17)),
                matchDate = LocalDate.parse(matchUri.slice(6..13), dateFormatter),
                url = "https://m.sports.naver.com${matchUri}"
            )
        }
        ?: emptyList()

}