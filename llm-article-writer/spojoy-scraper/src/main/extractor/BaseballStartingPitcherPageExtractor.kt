package io.gitp.llmarticlewrtier.spojoyscraper.extractor

import io.gitp.llmarticlewrtier.spojoyscraper.model.StartingPitcerPage
import kotlinx.serialization.json.*
import org.jsoup.nodes.Document


internal fun StartingPitcerPage.extractPitcherStats(): JsonObject {
    return buildJsonObject {
        put("homePitcher", extractPitcherInfo(this@extractPitcherStats.doc, "home"))
        put("awayPitcher", extractPitcherInfo(this@extractPitcherStats.doc, "away"))
    }
}

private fun extractPitcherInfo(document: Document, type: String): JsonObject {
    val pitcherDiv = document.select("#sp_info_$type").first()
    val seasonStats = extractThreeYearSeasonStats(document, type)
    val last10Games = extractLast10Games(document, type)
    val monthlyStats = extractMonthlyStats(document, type)

    return buildJsonObject {
        // 선수 기본 정보
        pitcherDiv?.let { div ->
            val name = div.select(".pitcher_player").text()
            val imageUrl = div.select(".pitcher_photo_box").attr("src")

            putJsonObject("basicInfo") {
                put("name", name.substringAfter(". "))
                put("number", name.substringBefore("."))
                put("imageUrl", imageUrl)

                div.select(".overcolor_light_data").first()?.let { details ->
                    val lines = details.text().split(":")

                    lines.forEach { line ->
                        val parts = line.trim().split(" ")
                        when {
                            line.contains("소속") -> put("team", line.substringAfter(":").trim())
                            line.contains("투타") -> put("throwsBats", line.substringAfter(":").trim())
                            line.contains("출생") -> put("birthDate", line.substringAfter(":").trim())
                            line.contains("신체") -> put("physicalInfo", line.substringAfter(":").trim())
                            line.contains("데뷔") -> put("debut", line.substringAfter(":").trim())
                        }
                    }
                }
            }

            // 시즌 성적과 방어율 정보
            val seasonStats = div.select(".big_data_font").map { it.text() }
            if (seasonStats.size >= 2) {
                put("seasonRecord", seasonStats[0])
                put("era", seasonStats[1])
            }

            // 홈/원정 성적
            div.select("td.overcolor_light_data").filter { foo -> foo.text().contains("승") }.forEach { td ->
                val text = td.text()
                if (text.contains("/")) {
                    val parts = text.split("/").map { it.trim() }
                    if (parts.size == 2) {
                        put("homeRecord", parts[0].replace("(", "").replace(")", ""))
                        put("awayRecord", parts[1].replace("(", "").replace(")", ""))
                    }
                }
            }
        }

        // 3년간 시즌 성적
        put("threeYearStats", seasonStats)

        // 최근 10경기 성적
        put("last10Games", last10Games)

        // 월별 성적
        put("monthlyStats", monthlyStats)
    }
}

private fun extractThreeYearSeasonStats(document: Document, type: String): JsonArray {
    val div = document.select("#sp_3season_$type").first()
    return buildJsonArray {
        div?.select("tr")?.filter { foo -> foo.select("td").size > 0 }?.forEach { row ->
            val cells = row.select("td")
            if (cells.size >= 13) {
                add(buildJsonObject {
                    put("season", cells[0].text())
                    put("games", cells[1].text())
                    put("wins", cells[2].text())
                    put("losses", cells[3].text())
                    put("saves", cells[4].text())
                    put("innings", cells[5].text())
                    put("runs", cells[6].text())
                    put("earnedRuns", cells[7].text())
                    put("hits", cells[8].text())
                    put("homeRuns", cells[9].text())
                    put("strikeouts", cells[10].text())
                    put("walks", cells[11].text())
                    put("era", cells[12].text())
                })
            }
        }
    }
}

private fun extractLast10Games(document: Document, type: String): JsonObject {
    val div = document.select("#sp_10games_$type").first()
    return buildJsonObject {
        // 10경기 통합 성적
        div?.select("tr")?.find { it.select("td:contains(10경기)").isNotEmpty() }?.let { row ->
            val cells = row.select("td")
            if (cells.size >= 12) {
                putJsonObject("summary") {
                    put("games", cells[0].text())
                    put("wins", cells[1].text())
                    put("losses", cells[2].text())
                    put("saves", cells[3].text())
                    put("innings", cells[4].text())
                    put("runs", cells[5].text())
                    put("earnedRuns", cells[6].text())
                    put("hits", cells[7].text())
                    put("homeRuns", cells[8].text())
                    put("strikeouts", cells[9].text())
                    put("walks", cells[10].text())
                    put("era", cells[11].text())
                }
            }
        }

        // 개별 경기 정보
        val games = buildJsonArray {
            div?.select("tr")?.filter { foo ->
                foo.select("a[href*=game_id]").isNotEmpty()
            }?.forEach { row ->
                val cells = row.select("td")
                if (cells.size >= 11) {
                    add(buildJsonObject {
                        put("date", cells[0].text())
                        put("homeAway", cells[1].text())
                        put("opponent", cells[2].text())
                        put("result", cells[3].text())
                        put("innings", cells[4].text())
                        put("runs", cells[5].text())
                        put("earnedRuns", cells[6].text())
                        put("hits", cells[7].text())
                        put("homeRuns", cells[8].text())
                        put("strikeouts", cells[9].text())
                        put("walks", cells[10].text())
                    })
                }
            }
        }
        put("games", games)
    }
}

private fun extractMonthlyStats(document: Document, type: String): JsonArray {
    val div = document.select("#sp_monthly_$type").first()
    return buildJsonArray {
        div?.select("tr")?.filter { tr -> tr.select("td").size > 0 }?.forEach { row ->
            val cells = row.select("td")
            if (cells.size >= 13) {
                add(buildJsonObject {
                    put("month", cells[0].text())
                    put("games", cells[1].text())
                    put("wins", cells[2].text())
                    put("losses", cells[3].text())
                    put("saves", cells[4].text())
                    put("innings", cells[5].text())
                    put("hits", cells[6].text())
                    put("homeRuns", cells[7].text())
                    put("walks", cells[8].text())
                    put("strikeouts", cells[9].text())
                    put("runs", cells[10].text())
                    put("earnedRuns", cells[11].text())
                    put("era", cells[12].text())
                })
            }
        }
    }
}

//
// private val logger = LoggerFactory.getLogger(object {}::class.java.packageName)
// private fun main() {
//     PlaywrightWorkerPool(4).use { browserWorkerPool ->
//         val baseballMatchListPage: Future<Document> = browserWorkerPool.submitTask {
//             logger.info("requesting1 spojoy mlb match list page(https://www.spojoy.com/live/?mct=baseball#rs)")
//             navigate("https://www.spojoy.com/live/?mct=baseball#rs")
//         }
//
//
//         val baseBallPageUrls = baseballMatchListPage.get()
//             .parseMlbMatchList()
//             .onEach { println(it) }
//
//         baseBallPageUrls
//             .map { baseBallPageUrl -> browserWorkerPool.submitTask { navigate(baseBallPageUrl) } }
//             .map { it.get() }
//             .map { baseBallPage -> BaseballStartingPitcherPageExtractor.extractPitcherStats(baseBallPage) }
//             .map { Json { prettyPrint = true }.encodeToString(it) }
//             .let { println(it) }
//     }
// }
