package io.gitp.sbpick.pickgenerator.scraper.basketballscraper

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.gitp.sbpick.pickgenerator.scraper.basketballscraper.models.BasketballMatchListPage
import io.gitp.sbpick.pickgenerator.scraper.basketballscraper.models.BasketballMatchPage
import io.gitp.sbpick.pickgenerator.scraper.basketballscraper.models.OneXTwoBetPage
import io.gitp.sbpick.pickgenerator.scraper.basketballscraper.models.OverUnderBetPage
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League

internal suspend fun PlaywrightBrowserPool.scrapeMatchListPage(league: League.Basketball): BasketballMatchListPage {
    return this
        .doAndGetDocAsync {
            logger.debug("scraping flashscore-basketball-match-list-page(url=${league.matchListPageUrl})")
            navigate(league.matchListPageUrl)
        }
        .await()
        .let { BasketballMatchListPage(it) }
}


internal suspend fun PlaywrightBrowserPool.scrapeMatchPage(matchPageUrl: String): BasketballMatchPage {
    return this
        .doAndGetDocAsync {
            logger.debug("scraping flashscore-basketball-match-page(url=${matchPageUrl})")
            navigate(matchPageUrl)
            listOf(
                "#detail  div.duelParticipant div.duelParticipant__home div.participant__participantNameWrapper div.participant__participantName.participant__overflow a",
                "#detail div.duelParticipant div.duelParticipant__away div.participant__participantNameWrapper div.participant__participantName.participant__overflow a"
            ).forEach { selector -> assertThat(locator(selector)).isVisible() }
        }
        .await()
        .let { BasketballMatchPage(it) }
}


internal suspend fun PlaywrightBrowserPool.scrapeOneXTwoBetPage(matchPageUrl: String): OneXTwoBetPage {
    return this
        .doAndGetDocAsync {
            logger.debug("scraping flashscore-basketball-1x2bet-page (matchPageUrl=${matchPageUrl})")
            navigate(matchPageUrl)
            locator(".detailOver a:nth-child(2)").click()
            locator(".wcl-tabs_jyS9b.wcl-tabsSecondary_SsnrA > a:nth-child(1)").click()
            assertThat(locator("#detail")).isVisible()
        }
        .await()
        .let { OneXTwoBetPage(it) }
}

internal suspend fun PlaywrightBrowserPool.scrapeOverUnderBetPage(matchPageUrl: String): OverUnderBetPage {
    return this
        .doAndGetDocAsync {
            logger.debug("scraping flashscore-basketball-over-under-bet-page (matchPageUrl=${matchPageUrl})")
            navigate(matchPageUrl)
            locator(".detailOver a:nth-child(2)").click()
            locator(".wcl-tabs_jyS9b.wcl-tabsSecondary_SsnrA > a:nth-child(3)").click()
            assertThat(locator("#detail")).isVisible()
        }
        .await()
        .let { OverUnderBetPage(it) }
}
