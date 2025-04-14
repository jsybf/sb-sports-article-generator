package io.gitp.llmarticlewriter.scraper.scrape

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.gitp.llmarticlewriter.scraper.PlaywrightBrowser
import io.gitp.llmarticlewriter.scraper.model.League
import io.gitp.llmarticlewriter.scraper.model.pages.common.CommonMatchUrlListPage
import io.gitp.llmarticlewriter.scraper.model.pages.common.CommonOneXTwoBetPage
import io.gitp.llmarticlewriter.scraper.model.pages.common.CommonOverUnderBetPage
import io.gitp.llmarticlewriter.scraper.model.pages.hockey.HockeyMatchPage

internal class HockeyScraper(
    private val browser: PlaywrightBrowser
) {


    fun requestUpcommingMatchListPage(league: League.Hockey): CommonMatchUrlListPage = browser
        .also { println("[INFO] requesting hockey-match-list (https://www.flashscore.co.kr/hockey/)") }
        .doAndGetDoc {
            navigate(league.matchListPageUrl)
        }
        .let { CommonMatchUrlListPage(it) }


    fun requestMatchPage(matchPageUrl: String): HockeyMatchPage = browser
        .also { println("[INFO] requesting hockey-match-summary ($matchPageUrl)") }
        .doAndGetDoc {
            navigate(matchPageUrl)
            assertThat(locator("#detail")).isVisible()
        }
        .let { HockeyMatchPage(it) }


    fun requestOneXTwoBetPage(matchPageUrl: String): CommonOneXTwoBetPage = browser
        .also { println("[INFO] requesting hockey-1x2-bet ($matchPageUrl)") }
        .doAndGetDoc {
            navigate(matchPageUrl)
            locator(".detailOver a:nth-child(2)").click()
            locator(".wcl-tabs_jyS9b.wcl-tabsSecondary_SsnrA > a:nth-child(1)").click()
            assertThat(locator("#detail")).isVisible()
        }
        .let { CommonOneXTwoBetPage(it) }

    fun requestOverUnderBetPage(matchPageUrl: String): CommonOverUnderBetPage = browser
        .also { println("[INFO] requesting hockey-totals-bet ($matchPageUrl)") }
        .doAndGetDoc {
            navigate(matchPageUrl)
            locator(".detailOver a:nth-child(2)").click()
            locator(".wcl-tabs_jyS9b.wcl-tabsSecondary_SsnrA > a:nth-child(3)").click()
            assertThat(locator("#detail")).isVisible()
        }
        .let { CommonOverUnderBetPage(it) }
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