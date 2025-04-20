package io.gitp.llmarticlewrtier.spojoyscraper.model

import kotlinx.serialization.json.JsonObject
import java.time.LocalDateTime

data class BaseballMatchInfo(
    val awayTeam: String,
    val homeTeam: String,
    val matchAt: LocalDateTime,
    // val league: League,
    val startingPitcherInfo: JsonObject,
    val batterInfo: JsonObject,
)


