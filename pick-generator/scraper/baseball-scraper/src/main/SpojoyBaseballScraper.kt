package io.gitp.sbpick.pickgenerator.scraper.baseballscraper

import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.models.*
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.net.URI


internal class SpoJoyBaseballScraper(
    private val browserPool: PlaywrightBrowserPool
) {
    suspend fun scrapeMatchListPage(): BaseballMatchListPage {
        val matchListPageURL = "https://www.spojoy.com/live/?mct=baseball"
        return this@SpoJoyBaseballScraper.browserPool
            .doAndGetDocAsync {
                logger.debug("scraping spojoy-baseball-match-list-page (url=${matchListPageURL})")
                navigate(matchListPageURL)
            }
            .await()
            .let { BaseballMatchListPage(it) }
    }

    suspend fun scrapeMatchPage(matchPageUrl: URI): BaseballMatchPage {
        return this.browserPool
            .doAndGetDocAsync {
                logger.debug("scraping spojoy-baseball-match-page (matchPageUrl=${matchPageUrl})")
                navigate(matchPageUrl.toString())
            }
            .await()
            .let { BaseballMatchPage(it) }
    }

    suspend fun scrapeStartingPitcherPage(matchPageUrl: URI): StartingPitcerPage {
        return this.browserPool
            .doAndGetDocAsync {
                logger.debug("scraping spojoy-baseball-staring-pitcher-page (matchPageUrl=${matchPageUrl})")
                navigate(matchPageUrl.toString())
            }
            .await()
            .let { StartingPitcerPage(it) }
    }

    suspend fun scrapePlayerListPage(matchPageUrl: URI): Pair<BaseballPlayerListPage, BaseballPlayerListPage> = coroutineScope {
        val baseballMatchPage = this@SpoJoyBaseballScraper.browserPool.doAndGetDocAsync {
            logger.debug("scraping spojoy-baseball-player-list-page (matchPageUrl=${matchPageUrl})")
            navigate(matchPageUrl.toString())
        }

        val (homePlayerListPageUrl, awayPlayerListPageUrl) = baseballMatchPage.await()
            .select("a.team_name_big")
            .also { aElements -> require(aElements.size == 2) }
            .map { "${it.attr("href")}&pgTk=players" }
            .let { Pair(it[0], it[1]) }


        val homePlayerListPage = async {
            this@SpoJoyBaseballScraper.browserPool
                .doAndGetDocAsync {
                    logger.debug("requesting spojoy-baseball-player-list-page (homeTeamPlayerListPageUrl=${homePlayerListPageUrl}")
                    navigate(homePlayerListPageUrl)
                }
                .await()
                .let { BaseballPlayerListPage(it) }
        }

        val awayPlayerListPage = async {
            this@SpoJoyBaseballScraper.browserPool
                .doAndGetDocAsync {
                    logger.debug("requesting spojoy-baseball-player-list-page (awayTeamPlayerListPageUrl=${awayPlayerListPageUrl}")
                    navigate(awayPlayerListPageUrl)
                }
                .await()
                .let { BaseballPlayerListPage(it) }
        }

        Pair(homePlayerListPage.await(), awayPlayerListPage.await())
    }

    suspend fun scrapePlayerPage(playerPageUrl: URI): BaseballPlayerPage {
        return this.browserPool
            .doAndGetDocAsync {
                logger.debug("requesting to spojoy-baseball-player-page ${playerPageUrl}")
                navigate(playerPageUrl.toString())
            }
            .await()
            .let { BaseballPlayerPage(it) }
    }
}

