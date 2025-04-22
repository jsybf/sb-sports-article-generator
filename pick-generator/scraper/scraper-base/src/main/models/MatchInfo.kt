package io.gitp.sbpick.pickgenerator.scraper.scrapebase.models

import java.time.LocalDateTime

interface MatchInfo {
    val homeTeam: String
    val awayTeam: String
    val matchAt: LocalDateTime
    val league: League
    val matchUniqueUrl: String
}
