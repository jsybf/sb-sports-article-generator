package io.gitp.sbpick.pickgenerator.scraper.scrapebase.models

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import java.net.URI

interface ScrapePipeline<T : MatchInfo, out L : League> {
    suspend fun getAllFixtureUrls(): List<URI>
    suspend fun getFixtureUrl(league: @UnsafeVariance L): List<URI>
    fun CoroutineScope.scrape(matchUrls: List<URI>): ReceiveChannel<T>
}