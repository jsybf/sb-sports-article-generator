package io.gitp.sbpick.pickgenerator.database

import io.gitp.sbpick.pickgenerator.database.VnlWomenSofaScrapedEntity.Dto
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.VnlWommenTeam
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.dao.UIntEntity
import org.jetbrains.exposed.dao.UIntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UIntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.json.json
import java.time.LocalDateTime

/**
 * table declaretions
 */

object SportsMatchTbl : UIntIdTable("sports_match", "sports_match_id") {
    val sports = varchar("sports", 20)
    val league = varchar("league", 10)
    val homeTeam = varchar("home_team", 30)
    val awayTeam = varchar("away_team", 30)
    val matchAt = datetime("match_at")
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    init {
        uniqueIndex(sports, league, homeTeam, awayTeam, matchAt)
    }
}

object PickTbl : UIntIdTable("pick", "pick_id") {
    val sportsMatchId = reference("sports_match_id", SportsMatchTbl)
    val content = text("content")
    val inputTokens = uinteger("input_tokens")
    val outputTokens = uinteger("output_tokens")
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    init {
        uniqueIndex(sportsMatchId)
    }
}

object VnlWomenMatchTbl : UIntIdTable("vnl_women_match", "vnl_women_match_id") {
    val homeTeam = enumerationByName<VnlWommenTeam>("home_team", 20)
    val awayTeam = enumerationByName<VnlWommenTeam>("away_team", 20)
    val matchAt = datetime("match_at")
    val sofascoreUrl = text("sofa_url")
    val flashscoreUrl = text("flash_url")
}

object VnlWommenSofaScrapedTbl : UIntIdTable("vnl_women_sofa_scraped", "vnl_women_sofa_scraped_id") {
    val matchId = reference("vnl_women_match_id", VnlWomenMatchTbl)
    val scraped = json<JsonObject>("scraped", Json)
    val updatedAt = timestamp("updated_at") // updated_at                TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
}

object VnlWomenPickTbl: UIntIdTable("vnl_women_pick", "vnl_women_pick_id") {
    val matchId = reference("vnl_women_match_id", VnlWomenMatchTbl)
    val pick = text("pick")
    val updatedAt = timestamp("updated_at") // updated_at                TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
}


/**
 * entity declaretions
 */

class SportsMatchEntity(id: EntityID<UInt>) : UIntEntity(id) {
    companion object : UIntEntityClass<SportsMatchEntity>(SportsMatchTbl)

    var sports by SportsMatchTbl.sports
    var league by SportsMatchTbl.league
    var homeTeam by SportsMatchTbl.homeTeam
    var awayTeam by SportsMatchTbl.awayTeam
    var matchAt by SportsMatchTbl.matchAt
    var updatedAt by SportsMatchTbl.updatedAt

    val pick: PickEntity? by PickEntity optionalBackReferencedOn PickTbl.sportsMatchId
}

class PickEntity(id: EntityID<UInt>) : UIntEntity(id) {
    companion object : UIntEntityClass<PickEntity>(PickTbl)

    var sportsMatch by SportsMatchEntity referencedOn PickTbl.sportsMatchId
    var content by PickTbl.content
    var inputTokens by PickTbl.inputTokens
    var outputTokens by PickTbl.outputTokens
    var updatedAt by PickTbl.updatedAt
}

class VnlWomenMatchEntity(id: EntityID<UInt>) : UIntEntity(id) {
    companion object : UIntEntityClass<VnlWomenMatchEntity>(VnlWomenMatchTbl)

    var homeTeam by VnlWomenMatchTbl.homeTeam
    var awayTeam by VnlWomenMatchTbl.awayTeam
    var matchAt by VnlWomenMatchTbl.matchAt
    var sofascoreUrl by VnlWomenMatchTbl.sofascoreUrl
    var flashscoreUrl by VnlWomenMatchTbl.flashscoreUrl

    data class Dto(
        val id: UInt?,
        val homeTeam: VnlWommenTeam,
        val awayTeam: VnlWommenTeam,
        val matchAt: LocalDateTime,
        val sofascoreUrl: String,
        val flashscoreUrl: String
    )

    fun toDto(): Dto = Dto(
        id = id.value,
        homeTeam = homeTeam,
        awayTeam = awayTeam,
        matchAt = matchAt,
        sofascoreUrl = sofascoreUrl,
        flashscoreUrl = flashscoreUrl
    )

}

class VnlWomenSofaScrapedEntity(id: EntityID<UInt>) : UIntEntity(id) {
    companion object : UIntEntityClass<VnlWomenSofaScrapedEntity>(VnlWommenSofaScrapedTbl)

    var match by VnlWomenMatchEntity referencedOn VnlWommenSofaScrapedTbl.matchId
    var scraped by VnlWommenSofaScrapedTbl.scraped

    data class Dto(
        val id: UInt?,
        val match: VnlWomenMatchEntity.Dto,
        val scraped: JsonObject
    )

    fun toDto(): Dto = Dto(
        id = id.value,
        match = match.toDto(),
        scraped = scraped
    )
}

// object VnlWomenPickTbl: UIntIdTable("vnl_women_pick", "vnl_women_pick_id") {
//     val matchId = reference("vnl_women_match_id", VnlWomenMatchTbl)
//     val pick = text("pick")
//     val updatedAt = timestamp("updated_at") // updated_at                TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
// }
class  VnlWomenPickEntity(id: EntityID<UInt>) : UIntEntity(id) {
    companion object : UIntEntityClass<VnlWomenPickEntity>(VnlWomenPickTbl)

    var match by VnlWomenMatchEntity referencedOn VnlWomenPickTbl.matchId
    var pick by VnlWomenPickTbl.pick

    data class Dto(
        val id: UInt?,
        val match: VnlWomenMatchEntity.Dto,
        val pick: String
    )

    fun toDto(): Dto = Dto(
        id = id.value,
        match = match.toDto(),
        pick= pick
    )
}


