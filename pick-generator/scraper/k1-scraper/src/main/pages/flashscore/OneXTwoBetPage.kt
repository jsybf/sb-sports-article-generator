package io.gitp.sbpick.pickgenerator.scraper.k1scraper.pages.flashscore

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.gitp.sbpick.pickgenerator.scraper.k1scraper.logger
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import org.jsoup.nodes.Document
import org.opentest4j.AssertionFailedError

data class FlashscoreOneXTwoBetPage(
    val doc: Document
)

@Serializable
data class OneXTwoOdds(
    val oneOdds: Float,
    val xOdds: Float,
    val twoOdds: Float
)

/**
 * @throws org.opentest4j.AssertionFailedError if button or expected element is not visible(not yet uploaded in source webpage)
 */
internal suspend fun PlaywrightBrowserPool.scrapeFlashscoreOneXTwoBetPage(matchPageUrl: String): Result<FlashscoreOneXTwoBetPage> = runCatching {
    this.doAndGetDocAsync {
        logger.debug("requesting flashscore soccer oneXtwoBet page (url=${matchPageUrl})")
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
        .await()
        .let { FlashscoreOneXTwoBetPage(it) }
}

internal fun FlashscoreOneXTwoBetPage.extractOdds(): List<OneXTwoOdds> {
    val rows = this@extractOdds.doc.select(".ui-table__body .ui-table__row")

    return rows
        .filter { row -> row.selectFirst("span.oddsCell__noOddsCell") == null }
        .map { row ->
            OneXTwoOdds(
                row.selectFirst("a[data-analytics-element]:nth-child(2) span")!!.text().toFloat(),
                row.selectFirst("a[data-analytics-element]:nth-child(3) span")!!.text().toFloat(),
                row.selectFirst("a[data-analytics-element]:nth-child(4) span")!!.text().toFloat()
            )
        }
}

/**
 * calcuate win, draw, loss probability based on 1x2 odds
 * @return (win_precentage, draw_precentage, loss_precentage)
 */
internal fun calculatePredictionFromOdds(odds: List<OneXTwoOdds>): Triple<Float, Float, Float> {
    val oneOddsAvg: Float = odds.map { it.oneOdds }.sum() / odds.size
    val xOddsAvg: Float = odds.map { it.xOdds }.sum() / odds.size
    val twoOddsAvg: Float = odds.map { it.twoOdds }.sum() / odds.size

    val paybackRate = 1 / (1 / oneOddsAvg + 1 / xOddsAvg + 1 / twoOddsAvg)

    val winPercent = (paybackRate * (1 / oneOddsAvg) * 100)
    val drawPercent = (paybackRate * (1 / xOddsAvg) * 100)
    val loosePercent = 100 - (winPercent + drawPercent)

    return Triple(winPercent, drawPercent, loosePercent)
}


suspend fun main() {
    val browserPool = PlaywrightBrowserPool(3)

    browserPool
        .scrapeFlashscoreFixturePage(League.KoreaSoccer.K1)
        .getOrThrow()
        .extractMatchInfo()
        .asFlow()
        .map { matchInfo ->
            browserPool.scrapeFlashscoreOneXTwoBetPage(matchInfo.flashscoreDetailPageUrl)
                .onFailure { e -> if (e is AssertionFailedError) logger.warn("oneXtwo odd page doesn't exist (url=${matchInfo.flashscoreDetailPageUrl})") else throw e }
                .map { it.extractOdds().let { calculatePredictionFromOdds(it) } }
        }
        .filter { it.isSuccess }
        .collect { println(it.getOrThrow()) }

    browserPool.close()
}
