package io.gitp.sbpick.pickgenerator.database.models

import io.gitp.sbpick.pickgenerator.database.SportsMatchEntity
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.MatchInfo
import java.time.LocalDateTime

internal fun SportsMatchEntity.toSportsMatchDto(): SportsMatchDto {
    val league = League.findByName(this.sports, this.league)!!
    return SportsMatchDto(
        id = this.id.value,
        league = league,
        homeTeam = this.homeTeam,
        awayTeam = this.awayTeam,
        matchAt = this.matchAt,
        matchUniqueUrl = matchUniqueUrl,
        updatedAt = this.matchAt,
    )
}

data class SportsMatchDto(
    val id: UInt?,
    val league: League,
    val homeTeam: String,
    val awayTeam: String,
    val matchAt: LocalDateTime,
    val matchUniqueUrl: String,
    val updatedAt: LocalDateTime?
) {
    companion object {
        fun from(matchInfo: MatchInfo): SportsMatchDto = SportsMatchDto(
            id = null,
            league = matchInfo.league,
            homeTeam = matchInfo.homeTeam,
            awayTeam = matchInfo.awayTeam,
            matchAt = matchInfo.matchAt,
            matchUniqueUrl = matchInfo.matchUniqueUrl,
            updatedAt = null
        )
    }
}


