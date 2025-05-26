package io.gitp.sbpick.pickgenerator.scraper.k1scraper.pages.flashscore

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.gitp.sbpick.pickgenerator.scraper.k1scraper.logger
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.*
import org.jsoup.nodes.Document
import org.opentest4j.AssertionFailedError
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal data class FlashscoreMatchPage(
    val doc: Document
)

internal suspend fun PlaywrightBrowserPool.scrapeFlashscoreMatchSummaryPage(matchPageUrl: String): Result<FlashscoreMatchPage> = runCatching {
    this
        .doAndGetDocAsync {
            logger.debug("requesting flashscore soccer match page (url=${matchPageUrl})")
            navigate(matchPageUrl)
            setOf(
                ".ui-table__body .ui-table__row"
            ).forEach { assertThat(locator(it).first()).isVisible() }
        }
        .await()
        .let { FlashscoreMatchPage(it) }
}

internal fun FlashscoreMatchPage.extractMatchSummary(): JsonObject = buildJsonObject {
    put("h2h", extractH2HRecord(this@extractMatchSummary.doc))
    put("rank_table", extractRankTable(this@extractMatchSummary.doc))

}

private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yy")


private fun extractH2HRecord(doc: Document): JsonArray = buildJsonArray {
    doc.select(".h2h__row").forEach { row ->
        val date = row.select(".h2h__date").text().let { LocalDate.parse(it, dateFormatter) }
        val league = row.select(".h2h__event").text()
        val homeTeam = row.select(".h2h__homeParticipant .h2h__participantInner").text()
        val awayTeam = row.select(".h2h__awayParticipant .h2h__participantInner").text()
        val score = row.select(".h2h__result span").let { "${it.first()!!.text()}:${it.last()!!.text()}" }
        addJsonObject {
            put("date", date.toString())
            put("competition", league)
            put("home_team", homeTeam)
            put("away_team", awayTeam)
            put("score", score)
        }
    }
}

private fun extractRankTable(doc: Document): JsonArray = buildJsonArray {
    doc.select(".ui-table__row").forEach { row ->
        addJsonObject {
            row.select(" .table__cell.table__cell--value").let { cells ->
                put("wins", cells[0].text())
                put("draws", cells[1].text())
                put("loose", cells[2].text())
                put("goal_differential", cells[3].text())
                put("match_points", cells[4].text())
            }
        }
    }

}

suspend fun main() {
    val browser = PlaywrightBrowserPool(1)
    browser
        .scrapeFlashscoreFixturePage(League.KoreaSoccer.K1)
        .getOrThrow()
        .extractMatchInfo()
        .take(4)
        .asFlow()
        .map { matchInfo ->
            browser.scrapeFlashscoreMatchSummaryPage(matchInfo.flashscoreDetailPageUrl)
                .onFailure { e -> if (e is AssertionFailedError) logger.warn("oneXtwo odd page doesn't exist (url=${matchInfo.flashscoreDetailPageUrl})") else throw e }
                .map { it.extractMatchSummary() }
        }
        .filter { it.isSuccess }
        .collect { Json { prettyPrint = true }.encodeToString(it.getOrThrow()).also { println(it) } }

    browser.close()
}