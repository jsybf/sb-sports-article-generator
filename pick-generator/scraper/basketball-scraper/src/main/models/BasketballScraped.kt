package io.gitp.sbpick.pickgenerator.scraper.basketballscraper.models

import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.LLMAttachment
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

internal data class BasketballScraped(
    val matchSummary: JsonObject,
    val oneXTwoBet: JsonArray,
    val overUnderBet: JsonArray,
) : LLMAttachment {
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
