package db

import db.dto.HockeyDto
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import scrape.hockey.HockeyPage

fun HockeyMatch.toDto(db: Database): HockeyDto.MatchPageSetDto = transaction(db) {
    val matchSummaryPage = this@toDto.summaryPages.first().let { HockeyPage.SummaryPage(it.page) }
    val absencePlayerPageList = this@toDto.playerPages.map { HockeyPage.PlayerPage(it.page) }

    HockeyDto.MatchPageSetDto(
        id = this@toDto.id.value,
        matchPageSet = HockeyPage.MatchPageSet(
            matchSummaryPage = matchSummaryPage,
            absencePlayerPageList = absencePlayerPageList
        )
    )
}
