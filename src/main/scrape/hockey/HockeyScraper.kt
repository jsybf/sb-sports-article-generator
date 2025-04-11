package scrape.hockey

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import scrape.PlaywrightBrowser

class HockeyScraper(
    private val browser: PlaywrightBrowser
) {
    private fun requestUpcommingMatchSummaryUrlList(): List<String> = browser
        .also { println("[INFO] requesting hockey-upcomming-match-list (https://www.flashscore.co.kr/hockey/)") }
        .doAndGetDoc {
            navigate("https://www.flashscore.co.kr/hockey/")
            locator("#live-table > div.filters > div.filters__group > div:nth-child(5) > div").click()
        }
        .select("section.event div.event__match > a")
        .map { it.attribute("href")?.value }
        .filterNotNull()

    private fun requestMatchSummaryDoc(url: String): HockeyPage.SummaryPage = browser
        .also { println("[INFO] requesting hockey-match-summary ($url)") }
        .doAndGetDoc {
            navigate(url)
            this.locator(".pending").all().forEach { assertThat(it).isHidden() }
        }
        .let { HockeyPage.SummaryPage(it) }

    private fun requestPlayerDoc(url: String): HockeyPage.PlayerPage = browser
        .also { println("[INFO] requesting hockey-player ($url)") }
        .doAndGetDoc { navigate(url) }
        .let { HockeyPage.PlayerPage(it) }

    fun scrapeAllUpcomingMatchList(): List<HockeyPage.MatchPageSet> {
        return this.requestUpcommingMatchSummaryUrlList().slice(0..1)
            .map { matchSummaryUrl: String ->
                val matchSumamary: HockeyPage.SummaryPage = requestMatchSummaryDoc(matchSummaryUrl)

                HockeyPage.MatchPageSet(
                    matchSummaryPage = matchSumamary,
                    absencePlayerPageList = matchSumamary.parseAbsencePlayerUrlList().map { url -> this.requestPlayerDoc(url) }
                )
            }
    }
}