package io.gitp.sbpick.pickgenerator.scraper.scrapebase.models

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import java.net.URI

interface ScrapePipeline<MI : MatchInfo, L : League> {
    suspend fun getFixtureUrl(league: L): List<URI>
    fun CoroutineScope.scrape(matchUrls: List<URI>): ReceiveChannel<Pair<MI, LLMAttachment>>
}