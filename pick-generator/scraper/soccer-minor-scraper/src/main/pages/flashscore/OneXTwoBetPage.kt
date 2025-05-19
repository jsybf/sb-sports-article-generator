package io.gitp.sbpick.pickgenerator.scraper.soccerminorscraper.pages.flashscore

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.soccerminorscraper.logger
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.jsoup.nodes.Document

data class FlashscoreOneXTwoBetPage(
    val doc: Document
)

/**
 * @throws org.opentest4j.AssertionFailedError if button or expected element is not visible(not yet uploaded in source webpage)
 */
internal suspend fun PlaywrightBrowserPool.scrapeFlashscoreOneXTwoBetPage(matchPageUrl: String): Result<FlashscoreOneXTwoBetPage> = runCatching {
    this.doAndGetDocAsync {
        navigate(matchPageUrl)
        locator("#detail div.detailOver  div a:nth-child(2)").click()
        locator(".wcl-tabs_jyS9b.wcl-tabsSecondary_SsnrA > a:nth-child(1)").click()
        setOf(
            ".ui-table__body .ui-table__row",
            "a[data-analytics-element]:nth-child(2) span",
            "a[data-analytics-element]:nth-child(3) span",
            "a[data-analytics-element]:nth-child(4) span",
        ).forEach { assertThat(locator(it).first()).isVisible() }
    }
        .also { logger.debug("requesting flashscore soccer oneXtwoBet page (url=${matchPageUrl})") }
        .await()
        .let { FlashscoreOneXTwoBetPage(it) }
}

internal fun FlashscoreOneXTwoBetPage.extractOdds(): JsonArray = buildJsonArray {
    val rows = this@extractOdds.doc.select(".ui-table__body .ui-table__row")

    rows
        .filter { row -> row.selectFirst("span.oddsCell__noOddsCell") == null }
        .forEach { row ->
            val oneXTwoOdd = buildJsonObject {
                put("oneOdds", row.selectFirst("a[data-analytics-element]:nth-child(2) span")!!.text())
                put("xOdds", row.selectFirst("a[data-analytics-element]:nth-child(3) span")!!.text())
                put("twoOdds", row.selectFirst("a[data-analytics-element]:nth-child(4) span")!!.text())
            }
            this.add(oneXTwoOdd)
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
//                 .scrapeFlashscoreOneXTwoBetPage(matchInfo.flashscoreDetailPageUrl)
//                 .onFailure { e -> if (e is AssertionFailedError) logger.warn("oneXtwo odd page doesn't exist (url=${matchInfo.flashscoreDetailPageUrl})") else throw e }
//                 .getOrNull()
//                 ?.extractOdds()
//         }
//         .filterNotNull()
//         .collect { println(it) }
//
//     browserPool.close()
// }
