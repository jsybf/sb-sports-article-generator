package db.repo

import db.*
import db.dto.HockeyDto
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import scrape.hockey.HockeyPage
import java.time.LocalDateTime

class HockeyRepo(
    private val db: Database
) {
    fun insertMatchPageSetAndGetId(pageSet: HockeyPage.MatchPageSet): Int = transaction(db) {
        val hockeyMatch = HockeyMatch.new {
            val (parsedHomeTeam, parsedAwayTeam) = pageSet.matchSummaryPage.parseTeam()
            startAt = pageSet.matchSummaryPage.parseStartDateTime()
            homeTeam = parsedHomeTeam
            awayTeam = parsedAwayTeam
            updatedAt = LocalDateTime.now()
        }
        HockeyMatchSummaryPage.new {
            this.hockeyMatch = hockeyMatch
            this.page = pageSet.matchSummaryPage.doc
        }
        pageSet.absencePlayerPageList.forEach { playerPage: HockeyPage.PlayerPage ->
            HockeyPlayerPage.new {
                this.hockeyMatch = hockeyMatch
                this.page = playerPage.doc
            }
        }

        hockeyMatch.id.value
    }

    fun findAllMatchPageSet(): List<HockeyDto.MatchPageSetDto> = transaction(db) {
        HockeyMatch.all().map { match -> match.toDto(db) }
    }

    fun insertLLMArticleAndGetId(matchId: Int, article: String): Int = transaction(db) {
        HockeyLLMArticle.new {
            hockeyMatch = HockeyMatch.findById(matchId) ?: throw Exception("row with (id:${matchId}) in (hockey_match) table unexist")
            this.article = article
            updatedAt = LocalDateTime.now()
        }.id.value
    }

    fun findLLMArticleAndGetId(matchId: Int): String? = transaction(db) { HockeyLLMArticle.findById(matchId)?.article }
}