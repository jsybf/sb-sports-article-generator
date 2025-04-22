package io.gitp.sbpick.pickgenerator.scraper.scrapebase.models

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

interface ScrapePipeline<out MI : MatchInfo, out L : League> {
    suspend fun getFixtureUrl(league: @UnsafeVariance L): List<String>
    fun CoroutineScope.scrape(matchUrls: List<String>): ReceiveChannel<Pair<MI, LLMAttachment>>
}