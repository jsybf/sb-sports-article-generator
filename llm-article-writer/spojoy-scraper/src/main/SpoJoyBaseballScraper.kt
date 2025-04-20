package io.gitp.llmarticlewrtier.spojoyscraper

import io.gitp.llmarticlewrtier.spojoyscraper.model.*
import org.jsoup.nodes.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

class SpoJoyBaseballScraper(
    private val playwrightWorkerPool: PlaywrightWorkerPool
) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun scrapeMatchListPage(): CompletableFuture<BaseballMatchListPage> {
        val matchListPageURL = "https://www.spojoy.com/live/?mct=baseball"
        return this.playwrightWorkerPool
            .submitTask {
                logger.info("scraping spojoy-baseball-match-list-page (url=${matchListPageURL})")
                navigate(matchListPageURL)
            }
            .let { doc: CompletableFuture<Document> -> doc.thenApply { BaseballMatchListPage(it) } }
    }

    fun scrapeMatchPage(matchPageUrl: String): CompletableFuture<BaseballMatchPage> {
        return this.playwrightWorkerPool
            .submitTask {
                logger.info("scraping spojoy-baseball-match-page (matchPageUrl=${matchPageUrl})")
                navigate(matchPageUrl)
            }
            .let { doc: CompletableFuture<Document> -> doc.thenApply { BaseballMatchPage(it) } }
    }

    fun scrapeStartingPitcherPage(matchPageUrl: String): CompletableFuture<StartingPitcerPage> {
        return this.playwrightWorkerPool
            .submitTask {
                logger.info("scraping spojoy-baseball-staring-pitcher-page (matchPageUrl=${matchPageUrl})")
                navigate(matchPageUrl)
            }
            .let { doc: CompletableFuture<Document> -> doc.thenApply { StartingPitcerPage(it) } }
    }

    fun scrapePlayerListPage(matchPageUrl: String): Pair<CompletableFuture<BaseballPlayerListPage>, CompletableFuture<BaseballPlayerListPage>> {
        val baseballMatchPage: CompletableFuture<Document> = this.playwrightWorkerPool.submitTask {
            logger.info("scraping spojoy-baseball-player-list-page (matchPageUrl=${matchPageUrl})")
            navigate(matchPageUrl)
        }

        val playerListPageUrls: CompletableFuture<Pair<String, String>> = baseballMatchPage.thenApply { matchPage ->
            matchPage
                .select("a.team_name_big")
                .also { aElements -> require(aElements.size == 2) }
                .map { "${it.attr("href")}&pgTk=players" }
                .let { Pair(it[0], it[1]) }
        }

        val homeTeamPlayerListPage: CompletableFuture<BaseballPlayerListPage> = playerListPageUrls.thenCompose { (homeTeamPlayerListPageUrl, _) ->
            this.playwrightWorkerPool.submitTask {
                logger.debug("requesting spojoy-baseball-player-list-page (homeTeamPlayerListPageUrl=${homeTeamPlayerListPageUrl}")
                navigate(homeTeamPlayerListPageUrl)
            }
        }.thenApply { BaseballPlayerListPage(it) }

        val awayTeamPlayerListPage: CompletableFuture<BaseballPlayerListPage> = playerListPageUrls.thenCompose { (_, awayTeamPlayerListPageUrl) ->
            this.playwrightWorkerPool.submitTask {
                logger.debug("requesting spojoy-baseball-player-list-page (awayTeamPlayerListPageUrl=${awayTeamPlayerListPageUrl}")
                navigate(awayTeamPlayerListPageUrl)
            }
        }.thenApply { BaseballPlayerListPage(it) }

        return Pair(homeTeamPlayerListPage, awayTeamPlayerListPage)
    }

    fun scrapePlayerPage(playerPageUrl: String): CompletableFuture<BaseballPlayerPage> {
        return this.playwrightWorkerPool
            .submitTask {
                logger.info("requesting to spojoy-baseball-player-page ${playerPageUrl}")
                navigate(playerPageUrl)
            }
            .let { docFuture -> docFuture.thenApply { BaseballPlayerPage(it) } }
    }
}

