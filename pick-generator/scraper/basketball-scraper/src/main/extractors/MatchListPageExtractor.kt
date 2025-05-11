package io.gitp.sbpick.pickgenerator.scraper.basketballscraper.extractors

import io.gitp.sbpick.pickgenerator.scraper.basketballscraper.models.BasketballMatchInfo
import io.gitp.sbpick.pickgenerator.scraper.basketballscraper.models.BasketballMatchListPage
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import java.time.LocalDateTime
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*


private val flashscoreDateTimeForamt = DateTimeFormatterBuilder()
    .appendPattern("dd.MM. HH:mm")
    .parseDefaulting(ChronoField.YEAR, LocalDateTime.now().year.toLong())
    .toFormatter()
    .withLocale(Locale.KOREAN)

fun BasketballMatchListPage.extractMatchInfo(): List<BasketballMatchInfo> {
    val league = this.doc
        .selectFirst(".heading__title .heading__name")!!.text()
        .let { League.findByName("basketball", it) ?: throw IllegalArgumentException("can't parse basketball team $it") }
        .let { it as League.Basketball }

    return this.doc
        .select(".event__match.event__match--withRowLink.event__match--static.event__match--scheduled.event__match--twoLine")
        .map { element ->
            val matchAt = element.selectFirst(".event__time")!!.text().let { LocalDateTime.parse(it, flashscoreDateTimeForamt) }
            val homeTeam = element.selectFirst(".event__participant.event__participant--home")!!.text()
            val awayTeam = element.selectFirst(".event__participant.event__participant--away")!!.text()
            val flashscoreDetailPage = element.selectFirst("a.eventRowLink")!!.attr("href")

            BasketballMatchInfo(
                awayTeam = awayTeam,
                homeTeam = homeTeam,
                matchAt = matchAt,
                league = league,
                flashscoreDetailPageUrl = flashscoreDetailPage
            )
        }
}
