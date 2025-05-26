package io.gitp.sbpick.pickgenerator.scraper.k1scraper.pages.flashscore

import io.gitp.sbpick.pickgenerator.scraper.k1scraper.logger
import io.gitp.sbpick.pickgenerator.scraper.k1scraper.models.K1MatchInfo
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import org.jsoup.nodes.Document
import java.time.LocalDateTime
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*

internal data class K1FixturePage(
    val doc: Document
)

internal suspend fun PlaywrightBrowserPool.scrapeFlashscoreFixturePage(league: League.KoreaSoccer): Result<K1FixturePage> = runCatching {
    this.doAndGetDocAsync {
        logger.debug("requesting flashscore soccer ${league} fixture list page (url=${league.fixtureListPageUrl})")
        navigate(league.fixtureListPageUrl)
    }
        .await()
        .let { K1FixturePage(it) }
}

private val flashscoreDateTimeForamt = DateTimeFormatterBuilder()
    .appendPattern("dd.MM. HH:mm")
    .parseDefaulting(ChronoField.YEAR, LocalDateTime.now().year.toLong())
    .toFormatter()
    .withLocale(Locale.KOREAN)


internal fun K1FixturePage.extractMatchInfo(): List<K1MatchInfo> {
    return this.doc
        .select(".event__match.event__match--withRowLink.event__match--static.event__match--scheduled.event__match--twoLine")
        .map { element ->
            val matchAt = element.selectFirst(".event__time")!!.text().let { LocalDateTime.parse(it, flashscoreDateTimeForamt) }
            val homeTeam = element.selectFirst(".event__homeParticipant")!!.text()
            val awayTeam = element.selectFirst(".event__awayParticipant")!!.text()
            val flashscoreDetailPage = element.selectFirst("a.eventRowLink")!!.attr("href")

            K1MatchInfo(
                awayTeam = awayTeam,
                homeTeam = homeTeam,
                matchAt = matchAt,
                league = League.KoreaSoccer.K1,
                flashscoreDetailPageUrl = flashscoreDetailPage
            )
        }
}