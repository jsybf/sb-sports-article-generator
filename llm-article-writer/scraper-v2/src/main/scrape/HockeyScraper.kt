package scrape

import PlaywrightBrowser
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import model.HockeyPage
import model.League

internal class HockeyScraper(
    private val browser: PlaywrightBrowser
) {


    fun requestUpcommingMatchListPage(league: League): HockeyPage.UpcommingMatcListhPage = browser
        .also { println("[INFO] requesting hockey-match-list (https://www.flashscore.co.kr/hockey/)") }
        .doAndGetDoc {
            navigate(league.url)
        }
        .let { HockeyPage.UpcommingMatcListhPage(it) }


    fun requestMatchPage(matchPageUrl: String): HockeyPage.MatchPage = browser
        .also { println("[INFO] requesting hockey-match-summary ($matchPageUrl)") }
        .doAndGetDoc {
            navigate(matchPageUrl)
            assertThat(locator("#detail")).isVisible()
        }
        .let { HockeyPage.MatchPage(it) }


    fun requestOneXTwoBetPage(matchPageUrl: String): HockeyPage.OneXTwoBetPage = browser
        .also { println("[INFO] requesting hockey-1x2-bet ($matchPageUrl)") }
        .doAndGetDoc {
            navigate(matchPageUrl)
            locator(".detailOver a:nth-child(2)").click()
            locator(".wcl-tabs_jyS9b.wcl-tabsSecondary_SsnrA > a:nth-child(1)").click()
            assertThat(locator("#detail")).isVisible()
        }
        .let { HockeyPage.OneXTwoBetPage(it) }

    fun requestOverUnderBetPage(matchPageUrl: String): HockeyPage.OverUnderBetPage = browser
        .also { println("[INFO] requesting hockey-totals-bet ($matchPageUrl)") }
        .doAndGetDoc {
            navigate(matchPageUrl)
            locator(".detailOver a:nth-child(2)").click()
            locator(".wcl-tabs_jyS9b.wcl-tabsSecondary_SsnrA > a:nth-child(3)").click()
            assertThat(locator("#detail")).isVisible()
        }
        .let { HockeyPage.OverUnderBetPage(it) }
}


// example
private fun main() {
    PlaywrightBrowser().use { browser ->
        val scraper = HockeyScraper(browser)
        scraper.requestOverUnderBetPage("https://www.flashscore.co.kr/match/hockey/GYgPihBG/#/match-summary")
            .also { println(it.doc.selectFirst("#detail")!!.html()) }
        scraper.requestOneXTwoBetPage("https://www.flashscore.co.kr/match/hockey/GYgPihBG/#/match-summary")
            .also { println(it.doc.selectFirst("#detail")!!.html()) }
    }
}