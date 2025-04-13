package io.gitp.llmarticlewriter.database.repo

import io.gitp.llmarticlewriter.database.*
import io.gitp.llmarticlewriter.database.dto.HockeyDto
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import scrape.hockey.HockeyPage
import java.time.LocalDateTime

class HockeyRepo(
    private val db: Database
) {
    fun ifMatchExist(matchPage: HockeyPage.SummaryPage): Boolean = transaction(db) {
        HockeyMatchTbl.selectAll()
            .andWhere { HockeyMatchTbl.startAt eq matchPage.parseStartDateTime() }
            .andWhere { HockeyMatchTbl.homeTeam eq matchPage.parseTeam().first }
            .andWhere { HockeyMatchTbl.awayTeam eq matchPage.parseTeam().second }
            .firstOrNull()
            .let { it != null }
    }

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

    fun findMatchPageSetNotHavingArticle(): List<HockeyDto.MatchPageSetDto> = transaction(db) {
        HockeyMatchTbl
            .leftJoin(HockeyLLMArticleTbl)
            .select(HockeyMatch.dependsOnColumns)
            .where { HockeyLLMArticleTbl.id eq null }
            .let { query -> HockeyMatch.wrapRows(query) }
            .map { entity -> entity.toDto(db) }
            .toList()
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