package io.gitp.sbpick.pickgenerator.scraper.soccerminorscraper.pages.flashscore

import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import io.gitp.sbpick.pickgenerator.scraper.soccerminorscraper.logger
import io.gitp.sbpick.pickgenerator.scraper.soccerminorscraper.models.FlashscoreMinorSoccerMatchInfo
import org.jsoup.nodes.Document
import java.time.LocalDateTime
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*

internal data class FlashscoreSoccerFixturePage(
    val doc: Document
)

internal suspend fun PlaywrightBrowserPool.scrapeFlashscoreFixturePage(league: League.MinorSoccer): Result<FlashscoreSoccerFixturePage> = runCatching {
    this.doAndGetDocAsync {
        logger.debug("requesting flashscore soccer ${league} fixture list page (url=${league.fixtureListPageUrl})")
        navigate(league.fixtureListPageUrl)
    }
        .await()
        .let { FlashscoreSoccerFixturePage(it) }
}

private val flashscoreDateTimeForamt = DateTimeFormatterBuilder()
    .appendPattern("dd.MM. HH:mm")
    .parseDefaulting(ChronoField.YEAR, LocalDateTime.now().year.toLong())
    .toFormatter()
    .withLocale(Locale.KOREAN)


internal fun FlashscoreSoccerFixturePage.extractMatchInfo(): List<FlashscoreMinorSoccerMatchInfo> {
    val league: League.MinorSoccer = this.doc
        .selectFirst(".heading__title .heading__name")!!.text()
        .let {
            when (it) {
                "WK리그" -> League.MinorSoccer.WK
                "K3 리그" -> League.MinorSoccer.K3
                "K리그 2" -> League.MinorSoccer.K2
                else -> throw IllegalArgumentException("can't match $it with League.MinorSoccer enum")
            }
        }

    return this.doc
        .select(".event__match.event__match--withRowLink.event__match--static.event__match--scheduled.event__match--twoLine")
        .map { element ->
            val matchAt = element.selectFirst(".event__time")!!.text().let { LocalDateTime.parse(it, flashscoreDateTimeForamt) }
            val homeTeam = element.selectFirst(".event__homeParticipant")!!.text()
            val awayTeam = element.selectFirst(".event__awayParticipant")!!.text()
            val flashscoreDetailPage = element.selectFirst("a.eventRowLink")!!.attr("href")

            FlashscoreMinorSoccerMatchInfo(
                awayTeam = awayTeam,
                homeTeam = homeTeam,
                matchAt = matchAt,
                league = league,
                flashscoreDetailPageUrl = flashscoreDetailPage
            )
        }

}