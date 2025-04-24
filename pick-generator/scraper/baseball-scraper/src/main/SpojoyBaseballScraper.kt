package io.gitp.sbpick.pickgenerator.scraper.baseballscraper

import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.models.*
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope


internal suspend fun PlaywrightBrowserPool.scrapeMatchListPage(): BaseballMatchListPage {
    val matchListPageURL = "https://www.spojoy.com/live/?mct=baseball"
    return this
        .doAndGetDocAsync {
            logger.debug("scraping spojoy-baseball-match-list-page (url=${matchListPageURL})")
            navigate(matchListPageURL)
        }
        .await()
        .let { BaseballMatchListPage(it) }
}

internal suspend fun PlaywrightBrowserPool.scrapeMatchPage(matchPageUrl: String): BaseballMatchPage {
    return this
        .doAndGetDocAsync {
            logger.debug("scraping spojoy-baseball-match-page (matchPageUrl=${matchPageUrl})")
            navigate(matchPageUrl)
        }
        .await()
        .let { BaseballMatchPage(it) }
}

internal suspend fun PlaywrightBrowserPool.scrapeStartingPitcherPage(matchPageUrl: String): StartingPitcerPage {
    return this
        .doAndGetDocAsync {
            logger.debug("scraping spojoy-baseball-staring-pitcher-page (matchPageUrl=${matchPageUrl})")
            navigate(matchPageUrl)
        }
        .await()
        .let { StartingPitcerPage(it) }
}

internal suspend fun PlaywrightBrowserPool.scrapePlayerListPage(matchPageUrl: String): Pair<BaseballPlayerListPage, BaseballPlayerListPage> = coroutineScope {
    val baseballMatchPage = this@scrapePlayerListPage
        .doAndGetDocAsync {
            logger.debug("scraping spojoy-baseball-player-list-page (matchPageUrl=${matchPageUrl})")
            navigate(matchPageUrl)
        }

    val (homePlayerListPageUrl, awayPlayerListPageUrl) = baseballMatchPage.await()
        .select("a.team_name_big")
        .also { aElements -> require(aElements.size == 2) }
        .map { "${it.attr("href")}&pgTk=players" }
        .let { Pair(it[0], it[1]) }


    val homePlayerListPage = async {
        this@scrapePlayerListPage
            .doAndGetDocAsync {
                logger.debug("requesting spojoy-baseball-player-list-page (homeTeamPlayerListPageUrl=${homePlayerListPageUrl}")
                navigate(homePlayerListPageUrl)
            }
            .await()
            .let { BaseballPlayerListPage(it) }
    }

    val awayPlayerListPage = async {
        this@scrapePlayerListPage
            .doAndGetDocAsync {
                logger.debug("requesting spojoy-baseball-player-list-page (awayTeamPlayerListPageUrl=${awayPlayerListPageUrl}")
                navigate(awayPlayerListPageUrl)
            }
            .await()
            .let { BaseballPlayerListPage(it) }
    }

    Pair(homePlayerListPage.await(), awayPlayerListPage.await())
}

internal suspend fun PlaywrightBrowserPool.scrapePlayerPage(playerPageUrl: String): BaseballPlayerPage {
    return this
        .doAndGetDocAsync {
            logger.debug("requesting to spojoy-baseball-player-page ${playerPageUrl}")
            navigate(playerPageUrl)
        }
        .await()
        .let { BaseballPlayerPage(it) }
}

