package io.gitp.sbpick.pickgenerator.database

import org.jetbrains.exposed.dao.UIntEntity
import org.jetbrains.exposed.dao.UIntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UIntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime


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

class SportsMatchEntity(id: EntityID<UInt>) : UIntEntity(id) {
    companion object : UIntEntityClass<SportsMatchEntity>(SportsMatchTbl)

    var sports by SportsMatchTbl.sports
    var league by SportsMatchTbl.league
    var homeTeam by SportsMatchTbl.homeTeam
    var awayTeam by SportsMatchTbl.awayTeam
    var matchAt by SportsMatchTbl.matchAt
    var updatedAt by SportsMatchTbl.updatedAt

    // 관계 매핑 - 해당 경기에 연결된 예측
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
