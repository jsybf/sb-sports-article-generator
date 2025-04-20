package io.gitp.llmarticlewrtier.spojoyscraper.extractor

import io.gitp.llmarticlewrtier.spojoyscraper.model.BaseballPlayerPage
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.jsoup.nodes.Document

fun BaseballPlayerPage.extractPlayerInfo(): JsonObject {
    val doc: Document = this.doc

    // 선수 기본 정보 추출
    val infoTable = doc.select("table[width=448]").first()
    val playerName = infoTable?.select("td:contains(이름)")?.next()?.text()?.trim() ?: ""
    val englishName = infoTable?.selectFirst("td:contains(이름)")?.nextElementSibling()?.select("br + *")?.text() ?: ""
    val birthDate = infoTable?.select("td:contains(생년월일)")?.next()?.text()?.trim() ?: ""
    val birthPlace = infoTable?.select("td:contains(출신)")?.next()?.text()?.trim() ?: ""
    val height = infoTable?.select("td:contains(신장)")?.next()?.text()?.trim() ?: ""
    val weight = infoTable?.select("td:contains(체중)")?.next()?.text()?.trim() ?: ""
    val team = infoTable?.select("td:contains(소속구단)")?.next()?.text()?.trim() ?: ""
    val position = infoTable?.select("td:contains(포지션)")?.next()?.text()?.trim() ?: ""

    // 통산 기록 추출 (최근 3년)
    val totalRecords = buildJsonArray {
        doc.select("table:contains(시즌):contains(경기수) tr").forEachIndexed { index, row ->
            if (index > 0) { // 헤더 제외
                val cells = row.select("td")
                if (cells.size >= 10) {
                    val record = buildJsonObject {
                        put("season", cells[0].text().trim())
                        put("league", cells[1].text().trim())
                        put("games", cells[2].text().trim().toIntOrNull() ?: 0)
                        put("hits", cells[3].text().trim().toIntOrNull() ?: 0)
                        put("homeRuns", cells[4].text().trim().toIntOrNull() ?: 0)
                        put("rbi", cells[5].text().trim().toIntOrNull() ?: 0)
                        put("runs", cells[6].text().trim().toIntOrNull() ?: 0)
                        put("walks", cells[7].text().trim().toIntOrNull() ?: 0)
                        put("strikeouts", cells[8].text().trim().toIntOrNull() ?: 0)
                        put("average", cells[9].text().trim().toDoubleOrNull() ?: 0.0)
                    }
                    this.add(record)
                }
            }
        }
    }

    // 최근 5경기 기록 추출
    val recentRecords = buildJsonArray {
        doc.select("table:contains(경기일시):contains(상대팀) tr").forEachIndexed { index, row ->
            if (index > 0) { // 헤더 제외
                val cells = row.select("td")
                if (cells.size >= 10) {
                    val record = buildJsonObject {
                        put("date", cells[0].text().trim())
                        put("league", cells[1].text().trim())
                        put("opponent", cells[2].text().trim())
                        put("hits", cells[3].text().trim().toIntOrNull() ?: 0)
                        put("homeRuns", cells[4].text().trim().toIntOrNull() ?: 0)
                        put("rbi", cells[5].text().trim().toIntOrNull() ?: 0)
                        put("runs", cells[6].text().trim().toIntOrNull() ?: 0)
                        put("walks", cells[7].text().trim().toIntOrNull() ?: 0)
                        put("strikeouts", cells[8].text().trim().toIntOrNull() ?: 0)
                        put("average", cells[9].text().trim().toDoubleOrNull() ?: 0.0)
                    }
                    this.add(record)
                }
            }
        }
    }

    // 전체 JSON 구성
    val playerJson = buildJsonObject {
        put("player", buildJsonObject {
            put("name", playerName)
            put("englishName", englishName.replace("(", "").replace(")", ""))
            put("birthDate", birthDate)
            put("birthPlace", birthPlace)
            put("height", height)
            put("weight", weight)
            put("team", team)
            put("position", position)
        })

        put("totalStatistics", totalRecords)
        put("recentGames", recentRecords)
    }
    return playerJson
}