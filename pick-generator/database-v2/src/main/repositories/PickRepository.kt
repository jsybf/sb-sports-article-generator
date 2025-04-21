package io.gitp.sbpick.pickgenerator.database.repositories

import io.gitp.sbpick.pickgenerator.database.PickTbl
import io.gitp.sbpick.pickgenerator.database.SportsMatchEntity
import io.gitp.sbpick.pickgenerator.database.SportsMatchTbl
import io.gitp.sbpick.pickgenerator.database.models.PickDto
import io.gitp.sbpick.pickgenerator.database.models.SportsMatchDto
import io.gitp.sbpick.pickgenerator.database.models.toPickDto
import io.gitp.sbpick.pickgenerator.database.models.toSportsMatchDto
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction


class PickRepository(
    private val db: Database
) {
    fun insertAndGetId(matchId: UInt, pick: PickDto): UInt = transaction(db) {
        PickTbl.insertAndGetId {
            it[sportsMatchId] = matchId
            it[content] = pick.content
            it[inputTokens] = pick.inputTokens
            it[outputTokens] = pick.outputTokens
        }.value
    }

    fun findMatchesHavingPick(): List<Pair<SportsMatchDto, PickDto>> = transaction(db) {
        val query = SportsMatchTbl
            .innerJoin(PickTbl)
            .select(SportsMatchEntity.dependsOnColumns)

        SportsMatchEntity
            .wrapRows(query)
            .map { sportsMatchEntity ->
                Pair(
                    sportsMatchEntity.toSportsMatchDto(),
                    sportsMatchEntity.pick!!.toPickDto()
                )
            }
    }
}