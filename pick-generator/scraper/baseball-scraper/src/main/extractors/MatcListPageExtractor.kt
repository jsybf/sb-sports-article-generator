package io.gitp.sbpick.pickgenerator.scraper.baseballscraper.extractors

import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.models.BaseballMatchListPage
import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.models.SpojoyBaseballMatchInfo
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.BaseballTeam
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * input은 아래와 같다
 * "javascript:openSpoDB(402775,'baseball','S','D');"
 * output은 402775(spojoy website의 mlb page id. 예시 https://spodb.spojoy.com/?game_id=402775 이런식)
 */
private val extractMatchIdFromHref = Regex("""[0-9]+""")
private val spojoyDateFormat = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")
private val extractSpojoyMatchTime = Regex("""(\d\d):(\d\d)""".trimMargin())

internal fun BaseballMatchListPage.extractMlbMatchList(): List<SpojoyBaseballMatchInfo> {
    val matchDate: LocalDate = this.doc
        .selectFirst("#ScheduleBox > table:nth-child(3) td.f9pt_gray3 b")!!.text()
        .slice(0..12)
        .let { LocalDate.parse(it, spojoyDateFormat) }

    val league = this.doc
        .selectFirst(".label_baseball")!!
        .text()
        .let {
            when (it) {
                "KBO" -> League.Baseball.KBO
                "NPB" -> League.Baseball.NPB
                "MLB" -> League.Baseball.MLB
                else -> throw IllegalStateException("can't parse baseball team(input=${it})")
            }
        }

    return this.doc
        .selectFirst("#ScheduleBox > table:nth-child(4)")!!
        .select("tr[onmouseover]")
        .filter { trElement -> trElement.select("#spodb_ a").text() == "비교분석" }
        .map { trElement ->
            val matchDateTime = trElement.selectFirst("td:nth-child(1)")!!.text()
                .let { extractSpojoyMatchTime.find(it) }!!
                .destructured
                .let { (hour, minute) ->
                    LocalDateTime.of(matchDate, LocalTime.of(hour.toInt(), minute.toInt()))
                }

            val homeTeam = trElement.selectFirst("#hnm_ a")!!.text().let { teamName ->
                when (league) {
                    League.Baseball.MLB -> BaseballTeam.MLBTeam.findByAnyCode(teamName)
                    League.Baseball.KBO -> BaseballTeam.KBOTeam.findByAnyCode(teamName)
                    League.Baseball.NPB -> BaseballTeam.NPBTeam.findByAnyCode(teamName)
                }
            }
            val awayTeam = trElement.selectFirst("#anm_ a")!!.text().let { teamName ->
                when (league) {
                    League.Baseball.MLB -> BaseballTeam.MLBTeam.findByAnyCode(teamName)
                    League.Baseball.KBO -> BaseballTeam.KBOTeam.findByAnyCode(teamName)
                    League.Baseball.NPB -> BaseballTeam.NPBTeam.findByAnyCode(teamName)
                }
            }

            val matchPageUrl = trElement.selectFirst("#spodb_ a")!!
                .attribute("href")!!.value
                .let { href -> extractMatchIdFromHref.find(href)!!.value }
                .let { mlbPageId -> "https://spodb.spojoy.com/?game_id=${mlbPageId}" }

            SpojoyBaseballMatchInfo(
                awayTeam = awayTeam.enumName(),
                homeTeam = homeTeam.enumName(),
                matchAt = matchDateTime,
                league = league,
                matchDetailPageUrl = matchPageUrl
            )
        }
}
