package io.gitp.llmarticlewriter.scraper.model

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import java.time.LocalDateTime

data class MatchInfo(
    val awayTeam: String,
    val homeTeam: String,
    val matchAt: LocalDateTime,
    val league: League,
    val matchPageUrl: String,
    val matchSummary: JsonObject,
    val oneXTwoBet: JsonArray,
    val overUnderBet: JsonArray,
)
