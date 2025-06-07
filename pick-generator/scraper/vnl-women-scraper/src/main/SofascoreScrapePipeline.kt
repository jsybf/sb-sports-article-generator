package io.gitp.sbpick.pickgenerator.scraper.vnlwommenscraper

import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.vnlwommenscraper.models.SofascoreScrapedResult
import io.gitp.sbpick.pickgenerator.scraper.vnlwommenscraper.pages.extractAudienceVotes
import io.gitp.sbpick.pickgenerator.scraper.vnlwommenscraper.pages.extractSetScores
import io.gitp.sbpick.pickgenerator.scraper.vnlwommenscraper.pages.extractStatisticsBox
import io.gitp.sbpick.pickgenerator.scraper.vnlwommenscraper.pages.scrapeSofascoreDetailPage
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement

private val jsonFormat = Json{ prettyPrint = true }

object SofascoreScrapePipeline {

    suspend fun scrapeMatchStatistics(browserPool: PlaywrightBrowserPool, url: String): Result<JsonObject> = runCatching {
        val detailPage = browserPool.scrapeSofascoreDetailPage(url)

        val statisctic = detailPage.extractStatisticsBox().getOrThrow()
        val audienceVote = detailPage.extractAudienceVotes().getOrThrow()
        val setScores = detailPage.extractSetScores().getOrThrow()

        val scrapedResult =  SofascoreScrapedResult.from(
            audienceVote = audienceVote,
            matchStatistics = statisctic,
            setScoreList = setScores
        )

        return@runCatching jsonFormat.encodeToJsonElement(scrapedResult) as JsonObject
    }
}