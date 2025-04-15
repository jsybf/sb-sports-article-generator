package io.gitp.llmarticlewriter.scraper.model.pages.common

import kotlinx.serialization.json.JsonArray
import org.jsoup.nodes.Document

data class CommonOneXTwoBetPage(
    val doc: Document
) {
    fun extractOdds(): JsonArray = with(CommonOneXTwoBetPageParser) { parseOdds() }
}