package io.gitp.sbpick.pickgenerator.database.repositories

import io.gitp.sbpick.pickgenerator.database.VnlWomenSofaScrapedEntity
import io.gitp.sbpick.pickgenerator.database.VnlWomenMatchEntity
import io.gitp.sbpick.pickgenerator.database.VnlWomenMatchTbl
import io.gitp.sbpick.pickgenerator.database.VnlWommenSofaScrapedTbl
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class VnlWomenSofascoreScrapedRepository(
    private val db: Database
) {
    fun insertScrapedAndGetId(matchId: UInt, scraped: JsonObject): UInt = transaction(db) {
        VnlWommenSofaScrapedTbl.insertAndGetId {
            it[VnlWommenSofaScrapedTbl.matchId] = matchId
            it[VnlWommenSofaScrapedTbl.scraped] = scraped
        }.value
    }

}

class VnlWomenRepository(
    private val db: Database
) {
    /**
     * 오늘 이전의 경기들중 sofascore의 통계정보들이 스크래핑 되어있지 않는 경기들 목록가져오기
     */
    fun findPrevMatchesNotHavingScraped(): List<VnlWomenMatchEntity.Dto> = transaction(db) {
        val query = VnlWomenMatchTbl
            .leftJoin(VnlWommenSofaScrapedTbl)
            .select(VnlWomenMatchEntity.dependsOnColumns)
            .where { VnlWommenSofaScrapedTbl.id eq null }

        VnlWomenMatchEntity
            .wrapRows(query)
            .map { entity -> entity.toDto() }
    }

    fun findMatchInfo(matchAt: LocalDateTime): List<VnlWomenSofaScrapedEntity.Dto> = transaction(db) {
        val query = VnlWomenMatchTbl
            .innerJoin(VnlWommenSofaScrapedTbl)
            .select(VnlWomenSofaScrapedEntity.dependsOnColumns)
            .where { VnlWomenMatchTbl.matchAt greaterEq matchAt }

        VnlWomenSofaScrapedEntity
            .wrapRows(query)
            .map { entity -> entity.toDto() }
    }

}