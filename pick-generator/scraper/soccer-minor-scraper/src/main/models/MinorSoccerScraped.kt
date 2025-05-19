package io.gitp.sbpick.pickgenerator.scraper.soccerminorscraper.models

import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.LLMAttachment
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

data class MinorSoccerScraped(
    val matchSummary: JsonObject,
    val oneXTwoBet: JsonArray,
    val overUnderBet: JsonArray,
) : LLMAttachment {
    override fun toLLMAttachment(): String = """
        <matchSummary>
        ${this.matchSummary}
        </matchSummary>
        <winOrLooseBets>
        ${this.oneXTwoBet}
        </winOrLooseBets>
        <totalScoreBets>
        ${this.overUnderBet}
        </totalScoreBets>
    """.trimIndent()
}
