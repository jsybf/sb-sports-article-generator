package io.gitp.sbpick.pickgenerator.scraper.basketballscraper

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.gitp.sbpick.pickgenerator.scraper.basketballscraper.models.BasketballMatchListPage
import io.gitp.sbpick.pickgenerator.scraper.basketballscraper.models.BasketballMatchPage
import io.gitp.sbpick.pickgenerator.scraper.basketballscraper.models.OneXTwoBetPage
import io.gitp.sbpick.pickgenerator.scraper.basketballscraper.models.OverUnderBetPage
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League

internal class FlashscoreBasketballScraper(
    private val browserPool: PlaywrightBrowserPool
) {
    suspend fun scrapeMatchListPage(league: League.Basketball): BasketballMatchListPage {
        return this.browserPool
            .doAndGetDocAsync {
                logger.debug("scraping flashscore-hockey-match-list-page(url=${league.matchListPageUrl})")
                navigate(league.matchListPageUrl.toString())
            }
            .await()
            .let { BasketballMatchListPage(it) }
    }


    suspend fun scrapeMatchPage(matchPageUrl: String): BasketballMatchPage {
        return this.browserPool
            .doAndGetDocAsync {
                logger.debug("scraping flashscore-hockey-match-page(url=${matchPageUrl})")
                navigate(matchPageUrl)
            }
            .await()
            .let { BasketballMatchPage(it) }
    }


    suspend fun scrapeOneXTwoBetPage(matchPageUrl: String): OneXTwoBetPage {
        return this.browserPool
            .doAndGetDocAsync {
                logger.debug("scraping flashscore-hockey-1x2bet-page (matchPageUrl=${matchPageUrl})")
                navigate(matchPageUrl)
                locator(".detailOver a:nth-child(2)").click()
                locator(".wcl-tabs_jyS9b.wcl-tabsSecondary_SsnrA > a:nth-child(1)").click()
                assertThat(locator("#detail")).isVisible()
            }
            .await()
            .let { OneXTwoBetPage(it) }
    }

    suspend fun scrapeOverUnderBetPage(matchPageUrl: String): OverUnderBetPage {
        return this.browserPool
            .doAndGetDocAsync {
                logger.debug("scraping flashscore-hockey-over-under-bet-page (matchPageUrl=${matchPageUrl})")
                navigate(matchPageUrl)
                locator(".detailOver a:nth-child(2)").click()
                locator(".wcl-tabs_jyS9b.wcl-tabsSecondary_SsnrA > a:nth-child(3)").click()
                assertThat(locator("#detail")).isVisible()
            }
            .await()
            .let { OverUnderBetPage(it) }
    }
}