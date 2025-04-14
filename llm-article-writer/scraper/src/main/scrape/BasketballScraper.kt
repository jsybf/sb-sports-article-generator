package scrape

import PlaywrightBrowser
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import model.Leaguee
import model.basketball.BasketBallMatchPage
import model.common.CommonMatchUrlListPage
import model.common.CommonOneXTwoBetPage
import model.common.CommonOverUnderBetPage

enum class BasketBallLeague(val url: String) {
    CBA("https://www.flashscore.co.kr/basketball/china/cba/fixtures/")
}

internal class BasketballScraper(
    private val browser: PlaywrightBrowser
) {
    fun requestUpcommingMatchListPage(league: Leaguee.BasketBall): CommonMatchUrlListPage = browser
        .also { println("[INFO] requesting ${league.name} (${league.matchListPageUrl})") }
        .doAndGetDoc {
            navigate(league.matchListPageUrl)
        }
        .let { CommonMatchUrlListPage(it) }

    fun requestMatchPage(matchPageUrl: String): BasketBallMatchPage = browser
        .also { println("[INFO] requesting hockey-match-summary ($matchPageUrl)") }
        .doAndGetDoc {
            navigate(matchPageUrl)
            assertThat(locator("#detail")).isVisible()
        }
        .let { BasketBallMatchPage(it) }

    fun requestOneXTwoBetPage(matchPageUrl: String): CommonOneXTwoBetPage = browser
        .also { println("[INFO] requesting basketball-1x2-bet ($matchPageUrl)") }
        .doAndGetDoc {
            navigate(matchPageUrl)
            locator(".detailOver a:nth-child(2)").click()
            locator(".wcl-tabs_jyS9b.wcl-tabsSecondary_SsnrA > a:nth-child(1)").click()
            assertThat(locator("#detail")).isVisible()
        }
        .let { CommonOneXTwoBetPage(it) }

    fun requestOverUnderBetPage(matchPageUrl: String): CommonOverUnderBetPage = browser
        .also { println("[INFO] requesting basketball-over-under-bet ($matchPageUrl)") }
        .doAndGetDoc {
            navigate(matchPageUrl)
            locator(".detailOver a:nth-child(2)").click()
            locator(".wcl-tabs_jyS9b.wcl-tabsSecondary_SsnrA > a:nth-child(3)").click()
            assertThat(locator("#detail")).isVisible()
        }
        .let { CommonOverUnderBetPage(it) }
}

private fun main() {
    PlaywrightBrowser().use { browser ->
        val scraper = BasketballScraper(browser)
        val matchUrlList = scraper.requestUpcommingMatchListPage(Leaguee.BasketBall.CBA).extractMatchUrls()

        matchUrlList
            .asSequence()
            .map { url -> scraper.requestMatchPage(url).extractMatchInfo() }
            .forEach { println(it) }

        //
        // resp
        //     .extractMatchUrls()
        //     .asSequence()
        //     .map { matchUrl -> scraper.requestOverUnderBetPage(matchUrl) }
        //     .map { overUnderBetPage -> overUnderBetPage.extractOdds() }
        //     .filter { overUnderBet -> overUnderBet.size != 0 }
        //     .onEach { overUnderBet -> println(overUnderBet) }
        //     .forEach { }
        // .map { matchUrl -> scraper.requestOneXTwoBetPage(matchUrl) }
        // .onEach { oneXTwoBetPage -> oneXTwoBetPage.extractOdds().let { println(it) } }
    }
}
