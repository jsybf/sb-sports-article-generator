package io.gitp.sbpick.pickgenerator.scraper.k1scraper.models

import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.LLMAttachment
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

data class K1Scraped(
    val oneXTwoOdds: JsonArray,
    val overUnderOdds: JsonArray,
    val majorPlayersDesc: JsonObject,
    val h2hRecord: JsonObject,
    val winDrawLoosePrediction: JsonObject,
    val overUnderPrediction: JsonObject
    // val topScorers: JsonArray
) : LLMAttachment {
    override fun toLLMAttachment(): String = """
        <oneXTwoOdds> 
            ${this.oneXTwoOdds}
        </oneXTwoOdds> 
        <overUnderOdds>
            ${this.overUnderOdds}
        </overUnderOdds>
        <majorPlayersDesc>
            ${this.majorPlayersDesc}
        </majorPlayersDesc>
        <h2hRecord>
            ${this.h2hRecord}
        </h2hRecord>
        <winDrawLoosePrediction>
            ${this.winDrawLoosePrediction}
        </winDrawLoosePrediction>
        <overUnderPrediction>
            ${this.overUnderPrediction}
        </overUnderPrediction>
    """.trimIndent()
}
