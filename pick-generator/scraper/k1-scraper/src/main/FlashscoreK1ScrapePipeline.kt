package io.gitp.sbpick.pickgenerator.scraper.k1scraper

import io.gitp.sbpick.pickgenerator.scraper.k1scraper.models.K1MatchInfo
import io.gitp.sbpick.pickgenerator.scraper.k1scraper.models.K1Scraped
import io.gitp.sbpick.pickgenerator.scraper.k1scraper.pages.flashscore.*
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.RequiredPageNotFound
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.K1Team
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.*
import org.opentest4j.AssertionFailedError
import java.time.LocalDate

object FlashscoreK1ScrapePipeline {
    suspend fun scrapeFixtures(browserPool: PlaywrightBrowserPool, league: League.KoreaSoccer, matchAt: LocalDate): List<K1MatchInfo> {
        logger.info("scraping flashscore soccer fixtures page(league=${league}, url=${league.fixtureListPageUrl})")
        return browserPool
            .scrapeFlashscoreFixturePage(league)
            .getOrThrow()
            .extractMatchInfo()
            .filter { it.matchAt.toLocalDate() == matchAt }
    }

    suspend fun scrapeMatch(browserPool: PlaywrightBrowserPool, matchInfo: K1MatchInfo): Result<K1Scraped> = coroutineScope {
        logger.info("scraping flashscore soccer match (matchInfo={${matchInfo}})")
        runCatching {
            val matchPageDeferred = async { browserPool.scrapeFlashscoreMatchSummaryPage(matchInfo.flashscoreDetailPageUrl) }
            val oneXTwoBetOddsPageDeferred = async { browserPool.scrapeFlashscoreOneXTwoBetPage(matchInfo.flashscoreDetailPageUrl) }
            val overUnderBetOddsPageDeferred = async { browserPool.scrapeFlashscoreOverUnderBetPage(matchInfo.flashscoreDetailPageUrl) }

            val matchPage = matchPageDeferred.await().onFailure { e ->
                if (e is AssertionFailedError) throw RequiredPageNotFound("flashscore match summary page ${matchInfo}") else throw e
            }
            val oneXTwoBetOddsPage = oneXTwoBetOddsPageDeferred.await().onFailure { e ->
                if (e is AssertionFailedError) throw RequiredPageNotFound("flashscore match 1x2bet page ${matchInfo}") else throw e
            }
            val overUnderBetOddsPage = overUnderBetOddsPageDeferred.await().onFailure { e ->
                if (e is AssertionFailedError) throw RequiredPageNotFound("flashscore match over_under_bet page ${matchInfo}") else throw e
            }

            val majorPlayDesc = buildJsonObject {
                put(
                    "home_major_player",
                    MajorPlayersContainer.findByTeamName(K1Team.fromTeamName(matchInfo.homeTeam)).random().let { Json.encodeToJsonElement(it) }
                )
                put(
                    "away_major_player",
                    MajorPlayersContainer.findByTeamName(K1Team.fromTeamName(matchInfo.awayTeam)).random().let { Json.encodeToJsonElement(it) }
                )
            }

            K1Scraped(
                oneXTwoOdds = oneXTwoBetOddsPage.getOrThrow().extractOdds().let { Json.encodeToJsonElement(it) }.jsonArray,
                overUnderOdds = overUnderBetOddsPage.getOrThrow().extractOdds().let { Json.encodeToJsonElement(it) }.jsonArray,
                majorPlayersDesc = majorPlayDesc,
                h2hRecord = matchPage.getOrThrow().extractMatchSummary(),
                winDrawLoosePrediction = oneXTwoBetOddsPage.getOrThrow().extractOdds().let { calculatePredictionFromOdds(it) }.let {
                    buildJsonObject {
                        put("home_win_precentage", it.first)
                        put("home_draw_precentage", it.second)
                        put("home_loose_precentage", it.third)
                    }
                },
                overUnderPrediction = overUnderBetOddsPage.getOrThrow().extractOdds().let { calculateOverUnderPredictionFromOdds(it.find { odds -> odds.totalScore == 2.5F }!!) }.let {
                    buildJsonObject {
                        put("over_percentage", it.first)
                        put("under_percentage", it.second)
                    }
                }
            )
        }
    }
}