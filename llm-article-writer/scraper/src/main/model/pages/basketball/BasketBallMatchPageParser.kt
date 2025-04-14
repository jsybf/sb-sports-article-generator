package io.gitp.llmarticlewriter.scraper.model.pages.basketball

import kotlinx.serialization.json.*
import org.jsoup.nodes.Document

object BasketBallMatchPageParser {
    fun BasketBallMatchPage.parseMatchInfo(): JsonObject = buildJsonObject {
        put("match", buildMatchInfo(doc))

        put("teams", buildTeamsInfo(doc))
        put("form", buildFormInfo(doc))
        put("h2h", buildH2HInfo(doc))
    }

    private fun buildMatchInfo(doc: Document): JsonObject {
        return buildJsonObject {
            // 경기 날짜/시간 추출
            val dateTime = doc.select(".duelParticipant__startTime div").text()
            put("date", JsonPrimitive(dateTime))

            // 경기장 정보 추출
            val venue = doc.select(".wcl-infoValue_0JeZb").first()?.text() ?: ""
            put("venue", JsonPrimitive(venue))

            // 심판 정보 추출
            val referees = doc.select(".wcl-infoValue_0JeZb").first()?.nextElementSibling()?.text() ?: ""
            put("referees", buildJsonArray {
                referees.split("-").forEach { referee ->
                    add(JsonPrimitive(referee.trim()))
                }
            })
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
        val standingsSelector = if (isHome) ".ui-table__row.table__row--selected:nth-of-type(7)" else ".ui-table__row.table__row--selected:nth-of-type(12)"
        val divisionSelector = if (isHome) ".tableWrapper:nth-of-type(2) .ui-table__row.table__row--selected:nth-of-type(3)" else ".tableWrapper:nth-of-type(2) .ui-table__row.table__row--selected:nth-of-type(6)"

        return buildJsonObject {
            // 팀 이름
            val name = doc.select("$selector .participant__participantName a").text()
            put("name", JsonPrimitive(name))

            // 팀 로고 URL
            val logoUrl = doc.select("$selector .participant__image").attr("src")
            put("logo", JsonPrimitive(logoUrl))

            // 팀 순위 (디비전)
            val divisionRank = doc.select(divisionSelector).select(".tableCellRank").text()
            put("rank", JsonPrimitive(divisionRank))

            // 리그 내 순위
            val leaguePosition = doc.select(standingsSelector).select(".tableCellRank").text().replace(".", "")
            put("league_position", JsonPrimitive(leaguePosition))
        }
    }

    private fun buildFormInfo(doc: Document): JsonObject {
        return buildJsonObject {
            // 홈팀 폼
            put("home", buildTeamForm(doc, ".detailTeamForm__team--home .detailTeamForm__vrp .wcl-trigger_YhU1j button"))

            // 원정팀 폼
            put("away", buildTeamForm(doc, ".detailTeamForm__team--away .detailTeamForm__vrp .wcl-trigger_YhU1j button"))
        }
    }


    private fun buildTeamForm(doc: Document, selector: String): JsonArray {
        val formElements = doc.select(selector)

        return buildJsonArray {
            formElements.forEach { element ->
                addJsonObject {
                    val result = when {
                        element.hasClass("wcl-win_9cMNW") -> "win"
                        element.hasClass("wcl-lose_qZrQy") -> "lose"
                        element.hasClass("wcl-drawWin_-7FBn") -> "draw_win"
                        element.hasClass("wcl-drawLose_pUe1j") -> "draw_lose"
                        else -> "unknown"
                    }
                    put("result", JsonPrimitive(result))
                }
            }
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
}


