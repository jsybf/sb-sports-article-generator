package io.gitp.llmarticlewrtier.spojoyscraper

import io.gitp.llmarticlewrtier.spojoyscraper.extractor.*
import io.gitp.llmarticlewrtier.spojoyscraper.model.BaseballMatchInfo
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

class SpojoyBaseballScrapePipeline(
    val browserPool: PlaywrightWorkerPool
) {
    private val scraper = SpoJoyBaseballScraper(browserPool)

    fun scrape(): Sequence<BaseballMatchInfo> {
        val matchPageUrls: List<String> = scraper.scrapeMatchListPage().get().parseMlbMatchList()


        return matchPageUrls
            .asSequence()
            .map { matchPageUrl ->
                val startingPitchersInfo = scraper.scrapeStartingPitcherPage(matchPageUrl).thenApply { it.extractPitcherStats() }
                val matchPage = scraper.scrapeMatchPage(matchPageUrl)
                // val (homeTeamPlayerListPage, awayTeamPlayerListPage) = scraper.scrapePlayerListPage(matchPageUrl)
                val (homeTeamPlayerPages, awayTeamPlayerPages) = scraper.scrapePlayerListPage(matchPageUrl)
                    .let { (homeTeamPlayerListPage, awayTeamPlayerListPage) ->
                        Pair(
                            homeTeamPlayerListPage.get()
                                .extractPlayerPageUrl()
                                .map { playerPageUrl -> scraper.scrapePlayerPage(playerPageUrl) },
                            awayTeamPlayerListPage.get()
                                .extractPlayerPageUrl()
                                .map { playerPageUrl -> scraper.scrapePlayerPage(playerPageUrl) },
                        )
                    }


                val (homeTeamName, awayTeamName) = matchPage.thenApply { it.extractTeamName() }.get()
                val matchAt = matchPage.thenApply { it.extractMatchAt() }.get()
                val battterInfo = buildJsonObject {
                    put(
                        "awayTeamBetter",
                        buildJsonArray {
                            awayTeamPlayerPages.map { it.get().extractPlayerInfo() }.forEach { add(it) }
                        }
                    )
                    put(
                        "homeTeamBetter",
                        buildJsonArray {
                            homeTeamPlayerPages.map { it.get().extractPlayerInfo() }.forEach { add(it) }
                        }
                    )
                }
                BaseballMatchInfo(
                    awayTeam = awayTeamName,
                    homeTeam = homeTeamName,
                    matchAt = matchAt,
                    startingPitcherInfo = startingPitchersInfo.get(),
                    batterInfo = battterInfo
                )
            }
            .onEach { println(it) }
    }
}

private fun main() = PlaywrightWorkerPool(4).use { browserPool ->
    val scrapePipline = SpojoyBaseballScrapePipeline(browserPool)
    scrapePipline.scrape().forEach { }

}
