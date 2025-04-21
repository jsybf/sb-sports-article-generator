package io.gitp.sbpick.pickgenerator.scraper.baseballscraper.extractors

import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.models.BaseballMatchPage
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * @return (home_team_name, away_team_name)
 */
internal fun BaseballMatchPage.extractTeamName(): Pair<String, String> =
    this.doc
        .select("""a.team_name_big""")
        .also { require(it.size == 2) }
        .map { it.text() }
        .let { Pair(it[0], it[1]) }


private val dateTimeStrImpurity = Regex("""\([가-힣]+\)""")
private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")

internal fun BaseballMatchPage.extractMatchAt(): LocalDateTime =
    this.doc
        .selectFirst(".top_info_box tr:nth-child(1) > td.top_contents_light")!!
        .text()
        .replace(dateTimeStrImpurity, "")
        .trim()
        .let { dateTimeStr -> java.time.LocalDateTime.parse(dateTimeStr, dateTimeFormatter) }

internal fun BaseballMatchPage.extractLeague(): League.Baseball =
    this.doc
        .selectFirst("""a[target="league"].game_name_big""")!!
        .text()
        .let { leaugeStr ->
            when {
                leaugeStr.contains("일본 프로야구") -> League.Baseball.NPB
                leaugeStr.contains("MLB") -> League.Baseball.MLB
                leaugeStr.contains("프로야구") -> League.Baseball.KBO
                else -> throw Exception("can't parse ${leaugeStr}")
            }
        }