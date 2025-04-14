package model

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import java.time.LocalDateTime

data class MatchInfo(
    val awayTeam: String,
    val homeTeam: String,
    val matchAt: LocalDateTime,
    val league: Leaguee,
    val matchPageUrl: String,
    val matchSummary: JsonObject,
    val oneXTwoBet: JsonArray,
    val overUnderBet: JsonArray,
)
