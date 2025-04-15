package io.gitp.llmarticlewriter.scraper.model.pages.common

import kotlinx.serialization.json.JsonArray
import org.jsoup.nodes.Document

class CommonOverUnderBetPage(
    val doc: Document
) {
    fun extractOdds(): JsonArray = with(CommonOverUnderBetPageParser) { parseOdds() }
}
