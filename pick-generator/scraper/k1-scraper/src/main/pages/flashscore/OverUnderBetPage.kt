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

data class FlashscoreOverUnderBetPage(
    val doc: Document
)

@Serializable
data class OverUnderOdds(
    val totalScore: Float,
    val overOdds: Float,
    val underodds: Float
)

/**
 * @throws org.opentest4j.AssertionFailedError if button or expected element is not visible(not yet uploaded in source webpage)
 */
internal suspend fun PlaywrightBrowserPool.scrapeFlashscoreOverUnderBetPage(matchPageUrl: String): Result<FlashscoreOverUnderBetPage> = runCatching {
    this.doAndGetDocAsync {
        navigate(matchPageUrl)
        logger.debug("requesting flashscore soccer overUnderBet page (url=${matchPageUrl})")
        locator("#detail div.detailOver  div a:nth-child(2)").click()
        locator(".wcl-tabs_jyS9b.wcl-tabsSecondary_SsnrA > a:nth-child(2)").click()
        assertThat(locator(".ui-table__body .ui-table__row").first()).isVisible()
        assertThat(locator("""a[data-analytics-element="ODDS_COMPARIONS_ODD_CELL_2"] span""").first()).isVisible()
    }
        .await()
        .let { FlashscoreOverUnderBetPage(it) }
}

internal fun FlashscoreOverUnderBetPage.extractOdds(): List<OverUnderOdds> {
    val rows = this@extractOdds.doc.select(".ui-table__body .ui-table__row")

    return rows
        .filter { row -> row.selectFirst("span.oddsCell__noOddsCell") == null }
        .map { row ->
            OverUnderOdds(
                row.selectFirst(".wcl-oddsInfo_wQfHM span")!!.text().toFloat(),
                row.selectFirst("""a[data-analytics-element="ODDS_COMPARIONS_ODD_CELL_2"] span""")!!.text().toFloat(),
                row.selectFirst("""a[data-analytics-element="ODDS_COMPARIONS_ODD_CELL_3"] span""")!!.text().toFloat()
            )
        }

}

/**
 * calcaulate over, under probability based on over under odds
 * @return (over_percent, under_percent)
 */
internal fun calculateOverUnderPredictionFromOdds(odds: OverUnderOdds): Pair<Float, Float> {
    val paybackRate = 1 / (1 / odds.overOdds + 1 / odds.underodds)
    val overPercent = paybackRate * (1 / odds.overOdds) * 100
    val underPercent = 100 - overPercent

    return Pair(overPercent, underPercent)
}

suspend fun main() {
    val browserPool = PlaywrightBrowserPool(3)

    browserPool
        .scrapeFlashscoreFixturePage(League.KoreaSoccer.K1)
        .getOrThrow()
        .extractMatchInfo()
        .asFlow()
        .map { matchInfo ->
            browserPool.scrapeFlashscoreOverUnderBetPage(matchInfo.flashscoreDetailPageUrl)
                .onFailure { e -> if (e is AssertionFailedError) logger.warn("oneXtwo odd page doesn't exist (url=${matchInfo.flashscoreDetailPageUrl})") else throw e }
                .map { it.extractOdds().let { calculateOverUnderPredictionFromOdds(it.find { it.totalScore == 2.5F }!!) } }
        }
        .filter { it.isSuccess }
        .collect { println(it.getOrThrow()) }

    browserPool.close()
}
