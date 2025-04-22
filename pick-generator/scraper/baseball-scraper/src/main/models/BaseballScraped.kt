package io.gitp.sbpick.pickgenerator.scraper.baseballscraper.models

import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.LLMAttachment
import kotlinx.serialization.json.JsonObject

data class BaseballScraped(
    val startingPitcherInfo: JsonObject,
    val batterInfo: JsonObject,
) : LLMAttachment {
    override fun toLLMAttachment(): String = """
        <startingPitcerInfo> 
        ${this.startingPitcherInfo}
        </startingPitcerInfo> 
        <batterInfo>
        ${this.batterInfo}
        </batterInfo>
    """.trimIndent()
}
