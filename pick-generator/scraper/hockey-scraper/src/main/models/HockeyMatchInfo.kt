package io.gitp.sbpick.pickgenerator.scraper.hockeyscraper.models

import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.LLMAttachment
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.MatchInfo
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import java.time.LocalDateTime

data class HockeyMatchInfo(
    override val awayTeam: String,
    override val homeTeam: String,
    override val matchAt: LocalDateTime,
    override val league: League,
    val matchSummary: JsonObject,
    val oneXTwoBet: JsonArray,
    val overUnderBet: JsonArray,
) : MatchInfo, LLMAttachment {

    override fun toLLMAttachment(): String = """
        <matchSummary>
        ${this.matchSummary}
        </matchSummary>
        <winOrLooseBetCurrent>
        ${this.oneXTwoBet}
        </winOrLooseBetCurrent>
        <totalScoreBetCurrent>
        ${this.overUnderBet}
        </totalScoreBetCurrent>
    """.trimIndent()

}
