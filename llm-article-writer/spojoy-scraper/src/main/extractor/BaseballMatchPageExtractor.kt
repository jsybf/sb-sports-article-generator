package io.gitp.llmarticlewrtier.spojoyscraper.extractor

import io.gitp.llmarticlewrtier.spojoyscraper.model.BaseballMatchPage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * @return (home_team_name, away_team_name)
 */
fun BaseballMatchPage.extractTeamName(): Pair<String, String> =
    this.doc
        .select("""a.team_name_big""")
        .also { require(it.size == 2) }
        .map { it.text() }
        .let { Pair(it[0], it[1]) }


private val dateTimeStrImpurity = Regex("""\([가-힣]+\)""")
private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")
fun BaseballMatchPage.extractMatchAt(): LocalDateTime =
    this.doc
        .selectFirst(".top_info_box tr:nth-child(1) > td.top_contents_light")!!
        .text()
        .replace(dateTimeStrImpurity, "")
        .trim()
        .let { dateTimeStr -> LocalDateTime.parse(dateTimeStr, dateTimeFormatter) }