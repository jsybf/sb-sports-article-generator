package model

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import org.jsoup.nodes.Document
import java.time.LocalDateTime

public enum class League(
    val url: String
) {
    NHL("https://www.flashscore.co.kr/hockey/usa/nhl/fixtures/"),
    KHL("https://www.flashscore.co.kr/hockey/russia/khl/fixtures/")
}

internal object HockeyPage {
    data class UpcommingMatcListhPage(
        val doc: Document
    )

    data class MatchPage(
        val doc: Document
    )

    data class OneXTwoBetPage(
        val doc: Document
    )

    data class OverUnderBetPage(
        val doc: Document
    )
}

data class HockeyMatchInfo(
    val awayTeam: String,
    val homeTeam: String,
    val matchAt: LocalDateTime,
    val league: League,
    val matchSummary: JsonObject,
    val oneXTwoBet: JsonArray,
    val overUnderBet: JsonArray,
)