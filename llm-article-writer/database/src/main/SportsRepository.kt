package io.gitp.llmarticlewriter.database

import io.gitp.llmarticlewriter.scraper.model.League
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime


data class MatchInfoDto(
    val id: Int?,
    val homeTeam: String,
    val awayTeam: String,
    val startAt: LocalDateTime,
    val league: League,
    val matchPageUrl: String,
)


data class HockeyScrapedPageDto(
    val id: Int?,
    val summary: String,
    val oneXTwoBet: String,
    val overUnderBet: String,
)

data class HockeyArticleDto(
    val id: Int?,
    val article: String,
    val inputTokens: Int,
    val outputTokens: Int
)

fun FlashScoreScrapedEntity.toDto() = HockeyScrapedPageDto(
    id = this.id.value,
    summary = this.summary,
    oneXTwoBet = this.oneXTwoBet,
    overUnderBet = this.overUnderBet,
)

fun SportsMatchEntity.toDto() = MatchInfoDto(
    id = this.id.value,
    homeTeam = this.homeTeam,
    awayTeam = this.awayTeam,
    startAt = this.startAt,
    league = League.ofName(this.sport, this.league),
    matchPageUrl = this.matchPageUrl
)

fun ArticleEntity.toDto() = HockeyArticleDto(
    id = this.id.value,
    article = this.article,
    inputTokens = this.inputTokens,
    outputTokens = this.outputTokens
)

class SportsRepository(
    private val db: Database
) {
    fun ifExists(homeTeam: String, awayTeam: String, startAt: LocalDateTime): Boolean = transaction(db) {
        SportsMatchTable
            .select(SportsMatchTable.id)
            .andWhere { SportsMatchTable.homeTeam eq homeTeam }
            .andWhere { SportsMatchTable.homeTeam eq awayTeam }
            .andWhere { SportsMatchTable.startAt eq startAt }
            .singleOrNull()
            .let { it != null }
    }

    fun insertHockeyMatch(matchInfoDto: MatchInfoDto): Int = transaction(db) {
        SportsMatchEntity.new {
            startAt = matchInfoDto.startAt
            homeTeam = matchInfoDto.homeTeam
            awayTeam = matchInfoDto.awayTeam
            updatedAt = LocalDateTime.now()
            sport = matchInfoDto.league.sportsName
            league = matchInfoDto.league.leagueName
            matchPageUrl = matchInfoDto.matchPageUrl
        }.id.value
    }

    fun insertHockeyScrapedPage(matchId: Int, dto: HockeyScrapedPageDto): Int = transaction(db) {
        FlashScoreScrapedTbl.insertAndGetId {
            it[hockeyMatchId] = matchId
            it[updatedAt] = LocalDateTime.now()
            it[summary] = dto.summary
            it[oneXTwoBet] = dto.oneXTwoBet
            it[overUnderBet] = dto.overUnderBet
        }.value
    }

    fun insertHockeyArticle(matchId: Int, dto: HockeyArticleDto): Int = transaction(db) {
        ArticleTbl.insertAndGetId {
            it[hockeyMatchId] = matchId
            it[updatedAt] = LocalDateTime.now()
            it[article] = dto.article
            it[inputTokens] = dto.inputTokens
            it[outputTokens] = dto.outputTokens
        }.value
    }

    fun findNotGeneratedMatches(): List<Pair<MatchInfoDto, HockeyScrapedPageDto>> = transaction(db) {
        SportsMatchTable
            .leftJoin(ArticleTbl)
            .select(SportsMatchEntity.dependsOnColumns)
            .where { ArticleTbl.id eq null }
            .let { SportsMatchEntity.wrapRows(it) }
            .map { matchEntity ->
                Pair(
                    matchEntity.toDto(),
                    matchEntity.scraped.toDto()
                )
            }
    }

    fun findMatchesHavingArticle(): List<Triple<MatchInfoDto, HockeyScrapedPageDto, HockeyArticleDto>> = transaction(db) {
        SportsMatchTable
            .innerJoin(ArticleTbl)
            .select(SportsMatchEntity.dependsOnColumns)
            .let { SportsMatchEntity.wrapRows(it) }
            .map { matchEntity ->
                Triple(
                    matchEntity.toDto(),
                    matchEntity.scraped.toDto(),
                    matchEntity.article!!.toDto()
                )
            }
    }
}