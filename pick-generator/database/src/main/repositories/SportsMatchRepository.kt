package io.gitp.sbpick.pickgenerator.database.repositories

import io.gitp.sbpick.pickgenerator.database.SportsMatchEntity
import io.gitp.sbpick.pickgenerator.database.SportsMatchTbl
import io.gitp.sbpick.pickgenerator.database.models.SportsMatchDto
import io.gitp.sbpick.pickgenerator.database.models.toSportsMatchDto
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class SportsMatchRepository(
    private val db: Database
) {
    fun insertMatchAndGetId(matchDto: SportsMatchDto): UInt = transaction(db) {
        SportsMatchTbl.insertAndGetId {
            it[sports] = matchDto.league.sportsName
            it[league] = matchDto.league.leagueName
            it[homeTeam] = matchDto.homeTeam
            it[awayTeam] = matchDto.awayTeam
            it[matchAt] = matchDto.matchAt
        }.value
    }

    fun findFixtures(): List<SportsMatchDto> = transaction(db) {
        val now = LocalDateTime.now()

        SportsMatchEntity
            .find { SportsMatchTbl.matchAt greaterEq now }
            .map { it.toSportsMatchDto() }
    }
}