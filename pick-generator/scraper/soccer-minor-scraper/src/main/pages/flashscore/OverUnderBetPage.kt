package io.gitp.sbpick.pickgenerator.scraper.soccerminorscraper.pages.flashscore

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.soccerminorscraper.logger
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.jsoup.nodes.Document

data class FlashscoreOverUnderBetPage(
    val doc: Document
)

/**
 * @throws org.opentest4j.AssertionFailedError if button or expected element is not visible(not yet uploaded in source webpage)
 */
internal suspend fun PlaywrightBrowserPool.scrapeFlashscoreOverUnderBetPage(matchPageUrl: String): Result<FlashscoreOverUnderBetPage> = runCatching {
    this.doAndGetDocAsync {
        navigate(matchPageUrl)
        locator("#detail div.detailOver  div a:nth-child(2)").click()
        locator(".wcl-tabs_jyS9b.wcl-tabsSecondary_SsnrA > a:nth-child(2)").click()
        assertThat(locator(".ui-table__body .ui-table__row").first()).isVisible()
        assertThat(locator("""a[data-analytics-element="ODDS_COMPARIONS_ODD_CELL_2"] span""").first()).isVisible()
    }
        .also { logger.debug("requesting flashscore soccer overUnderBet page (url=${matchPageUrl})") }
        .await()
        .let { FlashscoreOverUnderBetPage(it) }
}

internal fun FlashscoreOverUnderBetPage.extractOdds(): JsonArray = buildJsonArray {
    val rows = this@extractOdds.doc.select(".ui-table__body .ui-table__row")

    rows
        .filter { row -> row.selectFirst("span.oddsCell__noOddsCell") == null }
        .forEach { row ->
            val overUnderBet = buildJsonObject {
                put("total_score", row.selectFirst(".wcl-oddsInfo_wQfHM span")!!.text())
                put("over_odds", row.selectFirst("""a[data-analytics-element="ODDS_COMPARIONS_ODD_CELL_2"] span""")!!.text())
                put("under_odds", row.selectFirst("""a[data-analytics-element="ODDS_COMPARIONS_ODD_CELL_3"] span""")!!.text())
            }

            this.add(overUnderBet)
        }

}

// suspend fun main() {
//     val browserPool = PlaywrightBrowserPool(3)
//
//     browserPool
//         .scrapeFlashscoreFixturePage(League.MinorSoccer.K2)
//         .extractMatchInfo()
//         .asFlow()
//         .map { matchInfo ->
//             browserPool
//                 .scrapeFlashscoreOverUnderBetPage(matchInfo.flashscoreDetailPageUrl)
//                 .onFailure { e -> if (e is AssertionFailedError) logger.warn("oneXtwo odd page doesn't exist (url=${matchInfo.flashscoreDetailPageUrl})") else throw e }
//                 .getOrNull()
//                 ?.extractOdds()
//         }
//         .filterNotNull()
//         .collect { println(it) }
//
//     browserPool.close()
// }
