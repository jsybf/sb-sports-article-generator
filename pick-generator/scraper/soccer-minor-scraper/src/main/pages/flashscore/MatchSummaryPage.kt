package io.gitp.sbpick.pickgenerator.scraper.soccerminorscraper.pages.flashscore

import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.soccerminorscraper.logger
import kotlinx.serialization.json.*
import org.jsoup.nodes.Document

data class FlashscoreMatchPage(
    val doc: Document
)

internal suspend fun PlaywrightBrowserPool.scrapeFlashscoreMatchSummaryPage(matchPageUrl: String): Result<FlashscoreMatchPage> = runCatching {
    this
        .doAndGetDocAsync {
            logger.debug("requesting flashscore soccer match page (url=${matchPageUrl})")
            navigate(matchPageUrl)
        }
        .await()
        .let { FlashscoreMatchPage(it) }
}


internal fun FlashscoreMatchPage.extractMatchInfo(): JsonObject = buildJsonObject {
    put("match", buildMatchInfo(doc))
    put("teams", buildTeamsInfo(doc))
    put("h2h", buildH2HInfo(doc))
    put("ranking_board", extractRankTable(doc))
}

private fun buildMatchInfo(doc: Document): JsonObject {
    return buildJsonObject {
        // 경기 날짜/시간 추출
        val dateTime = doc.select(".duelParticipant__startTime div").text()
        put("date", JsonPrimitive(dateTime))

        // 경기장 정보 추출
        val venue = doc.select(".wcl-infoValue_0JeZb").first()?.text() ?: ""
        put("venue", JsonPrimitive(venue))
    }
}

private fun buildTeamsInfo(doc: Document): JsonObject {
    return buildJsonObject {
        // 홈팀 정보
        put("home", buildTeamInfo(doc, true))

        // 원정팀 정보
        put("away", buildTeamInfo(doc, false))
    }
}

private fun buildTeamInfo(doc: Document, isHome: Boolean): JsonObject {
    val selector = if (isHome) ".duelParticipant__home" else ".duelParticipant__away"

    return buildJsonObject {
        // 팀 이름
        val name = doc.select("$selector .participant__participantName a").text()
        put("name", JsonPrimitive(name))
    }
}

private fun buildH2HInfo(doc: Document): JsonArray {
    val h2hRows = doc.select(".h2h__row")

    return buildJsonArray {
        h2hRows.take(3).forEach { row ->
            addJsonObject {
                val date = row.select(".h2h__date").text()
                val competition = row.select(".h2h__event").text()
                val homeTeam = row.select(".h2h__homeParticipant .h2h__participantInner").text()
                val awayTeam = row.select(".h2h__awayParticipant .h2h__participantInner").text()
                val score = row.select(".h2h__result span").let {
                    "${it.first()!!.text()}:${it.last()!!.text()}"
                }
                val regularTime = row.select(".h2h__result__fulltime .h2h__regularTimeResult").let {
                    if (it.size >= 2) "${it.first()!!.text()}:${it.last()!!.text()}" else ""
                }

                put("date", JsonPrimitive(date))
                put("competition", JsonPrimitive(competition))
                put("home_team", JsonPrimitive(homeTeam))
                put("away_team", JsonPrimitive(awayTeam))
                put("score", JsonPrimitive(score))

                if (regularTime.isNotEmpty()) {
                    put("regular_time", JsonPrimitive(regularTime))
                }
            }
        }
    }
}

private fun extractRankTable(doc: Document): JsonObject {
    val tableRows = doc.select(".tableWrapper:first-of-type .ui-table__row")
    return buildJsonObject {
        putJsonArray("teams") {
            tableRows.forEach { row ->
                addJsonObject {
                    val position = row.select(".tableCellRank").text().replace(".", "").toIntOrNull() ?: 0
                    val name = row.select(".tableCellParticipant__name").text()
                    val cells = row.select(".table__cell--value")

                    put("position", JsonPrimitive(position))
                    put("name", JsonPrimitive(name))
                    put("games_played", JsonPrimitive(cells[0].text().toIntOrNull() ?: 0))
                    put("wins", JsonPrimitive(cells[1].text().toIntOrNull() ?: 0))
                    put("ot_wins", JsonPrimitive(cells[2].text().toIntOrNull() ?: 0))
                    put("ot_losses", JsonPrimitive(cells[3].text().toIntOrNull() ?: 0))
                    put("losses", JsonPrimitive(cells[4].text().toIntOrNull() ?: 0))
                    put("goals", JsonPrimitive(cells[5].text()))
                    put("points", JsonPrimitive(cells[6].text().toIntOrNull() ?: 0))
                }
            }
        }
    }
}