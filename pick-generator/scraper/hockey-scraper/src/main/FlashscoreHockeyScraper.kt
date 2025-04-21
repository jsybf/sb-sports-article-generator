package io.gitp.sbpick.pickgenerator.scraper.hockeyscraper

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.gitp.sbpick.pickgenerator.scraper.hockeyscraper.models.HockeyMatchListPage
import io.gitp.sbpick.pickgenerator.scraper.hockeyscraper.models.HockeyMatchPage
import io.gitp.sbpick.pickgenerator.scraper.hockeyscraper.models.OneXTwoBetPage
import io.gitp.sbpick.pickgenerator.scraper.hockeyscraper.models.OverUnderBetPage
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import java.net.URI

internal class FlashscoreHockeyScraper(
    private val browserPool: PlaywrightBrowserPool
) {
    suspend fun scrapeMatchListPage(league: League.Hockey): HockeyMatchListPage {
        return this.browserPool
            .doAndGetDocAsync {
                logger.debug("scraping flashscore-hockey-match-list-page(url=${league.matchListPageUrl})")
                navigate(league.matchListPageUrl.toString())
            }
            .await()
            .let { HockeyMatchListPage(it) }
    }


    suspend fun scrapeMatchPage(matchPageUrl: URI): HockeyMatchPage {
        return this.browserPool
            .doAndGetDocAsync {
                logger.debug("scraping flashscore-hockey-match-page(url=${matchPageUrl})")
                navigate(matchPageUrl.toString())
            }
            .await()
            .let { HockeyMatchPage(it) }
    }


    suspend fun scrapeOneXTwoBetPage(matchPageUrl: URI): OneXTwoBetPage {
        return this.browserPool
            .doAndGetDocAsync {
                logger.debug("scraping flashscore-hockey-1x2bet-page (matchPageUrl=${matchPageUrl})")
                navigate(matchPageUrl.toString())
                locator(".detailOver a:nth-child(2)").click()
                locator(".wcl-tabs_jyS9b.wcl-tabsSecondary_SsnrA > a:nth-child(1)").click()
                assertThat(locator("#detail")).isVisible()
            }
            .await()
            .let { OneXTwoBetPage(it) }
    }

    suspend fun scrapeOverUnderBetPage(matchPageUrl: URI): OverUnderBetPage {
        return this.browserPool
            .doAndGetDocAsync {
                logger.debug("scraping flashscore-hockey-over-under-bet-page (matchPageUrl=${matchPageUrl})")
                navigate(matchPageUrl.toString())
                locator(".detailOver a:nth-child(2)").click()
                locator(".wcl-tabs_jyS9b.wcl-tabsSecondary_SsnrA > a:nth-child(3)").click()
                assertThat(locator("#detail")).isVisible()
            }
            .await()
            .let { OverUnderBetPage(it) }
    }
}