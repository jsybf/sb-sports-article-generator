package io.gitp.sbpick.pickgenerator.scraper.scrapebase.models

import java.time.LocalDateTime

interface MatchInfo {
    val homeTeam: String
    val awayTeam: String
    val matchAt: LocalDateTime
    val league: League

    fun isEqual(other: MatchInfo): Boolean {
        return this.homeTeam == other.homeTeam
                && this.awayTeam == other.awayTeam
                && this.matchAt.toLocalDate() == other.matchAt.toLocalDate()
                && this.league == other.league
    }
}
