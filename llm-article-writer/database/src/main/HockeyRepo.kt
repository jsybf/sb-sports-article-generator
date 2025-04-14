package io.gitp.llmarticlewriter.database

import model.League
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime


data class HockeyMatchDto(
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

fun HockeyScrapedEntity.toDto() = HockeyScrapedPageDto(
    id = this.id.value,
    summary = this.summary,
    oneXTwoBet = this.oneXTwoBet,
    overUnderBet = this.overUnderBet,
)

fun HockeyMatchEntity.toDto() = HockeyMatchDto(
    id = this.id.value,
    homeTeam = this.homeTeam,
    awayTeam = this.awayTeam,
    startAt = this.startAt,
    league = this.league,
    matchPageUrl = this.matchPageUrl
)

fun HockeyArticleEntity.toDto() = HockeyArticleDto(
    id = this.id.value,
    article = this.article,
    inputTokens = this.inputTokens,
    outputTokens = this.outputTokens
)

class HockeyRepo(
    private val db: Database
) {
    fun ifExists(homeTeam: String, awayTeam: String, startAt: LocalDateTime): Boolean = transaction(db) {
        HockeyMatchTbl
            .select(HockeyMatchTbl.id)
            .andWhere { HockeyMatchTbl.homeTeam eq homeTeam }
            .andWhere { HockeyMatchTbl.homeTeam eq awayTeam }
            .andWhere { HockeyMatchTbl.startAt eq startAt }
            .singleOrNull()
            .let { it != null }
    }

    fun insertHockeyMatch(hockeyMatchDto: HockeyMatchDto): Int = transaction(db) {
        HockeyMatchEntity.new {
            startAt = hockeyMatchDto.startAt
            homeTeam = hockeyMatchDto.homeTeam
            awayTeam = hockeyMatchDto.awayTeam
            updatedAt = LocalDateTime.now()
            league = hockeyMatchDto.league
            matchPageUrl = hockeyMatchDto.matchPageUrl
        }.id.value
    }

    fun insertHockeyScrapedPage(matchId: Int, dto: HockeyScrapedPageDto): Int = transaction(db) {
        HockeyScrapedTbl.insertAndGetId {
            it[hockeyMatchId] = matchId
            it[updatedAt] = LocalDateTime.now()
            it[summary] = dto.summary
            it[oneXTwoBet] = dto.oneXTwoBet
            it[overUnderBet] = dto.overUnderBet
        }.value
    }

    fun insertHockeyArticle(matchId: Int, dto: HockeyArticleDto): Int = transaction(db) {
        HockeyArticleTbl.insertAndGetId {
            it[hockeyMatchId] = matchId
            it[updatedAt] = LocalDateTime.now()
            it[article] = dto.article
            it[inputTokens] = dto.inputTokens
            it[outputTokens] = dto.outputTokens
        }.value
    }

    fun findNotGeneratedMatches(): List<Pair<HockeyMatchDto, HockeyScrapedPageDto>> = transaction(db) {
        HockeyMatchTbl
            .leftJoin(HockeyArticleTbl)
            .select(HockeyMatchEntity.dependsOnColumns)
            .where { HockeyArticleTbl.id eq null }
            .let { HockeyMatchEntity.wrapRows(it) }
            .map { matchEntity ->
                Pair(
                    matchEntity.toDto(),
                    matchEntity.scraped.toDto()
                )
            }
    }

    fun findMatchesHavingArticle(): List<Triple<HockeyMatchDto, HockeyScrapedPageDto, HockeyArticleDto>> = transaction(db) {
        HockeyMatchTbl
            .innerJoin(HockeyArticleTbl)
            .select(HockeyMatchEntity.dependsOnColumns)
            .let { HockeyMatchEntity.wrapRows(it) }
            .map { matchEntity ->
                Triple(
                    matchEntity.toDto(),
                    matchEntity.scraped.toDto(),
                    matchEntity.article!!.toDto()
                )
            }
    }
}