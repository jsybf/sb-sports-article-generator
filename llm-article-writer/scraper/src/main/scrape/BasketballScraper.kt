package io.gitp.llmarticlewriter.scraper.scrape

import com.microsoft.playwright.CLI
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.gitp.llmarticlewriter.scraper.PlaywrightBrowser
import io.gitp.llmarticlewriter.scraper.model.League
import io.gitp.llmarticlewriter.scraper.model.pages.basketball.BasketBallMatchPage
import io.gitp.llmarticlewriter.scraper.model.pages.common.CommonMatchUrlListPage
import io.gitp.llmarticlewriter.scraper.model.pages.common.CommonOneXTwoBetPage
import io.gitp.llmarticlewriter.scraper.model.pages.common.CommonOverUnderBetPage


internal class BasketballScraper(
    private val browser: PlaywrightBrowser
) {
    fun requestUpcommingMatchListPage(league: League.BasketBall): CommonMatchUrlListPage = browser
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
        val matchUrlList = scraper.requestUpcommingMatchListPage(League.BasketBall.CBA).extractMatchUrls()

        matchUrlList
            .asSequence()
            .map { url -> scraper.requestMatchPage(url).extractMatchInfo() }
            .forEach { println(it) }
    }
}
