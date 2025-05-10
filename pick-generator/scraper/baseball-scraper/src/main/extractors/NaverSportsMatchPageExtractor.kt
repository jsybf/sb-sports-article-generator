package io.gitp.sbpick.pickgenerator.scraper.baseballscraper.extractors

import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.models.NaverSportsBaseballMatchPage
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.RequiredPageNotFound
import kotlinx.serialization.json.*


internal fun NaverSportsBaseballMatchPage.extractStaringPitcherStatistics(): JsonObject = buildJsonObject {
    val startPitcherTblElement = this@extractStaringPitcherStatistics.doc.selectFirst("table.StartPlayer_data_table__Q1TvG")
        ?: throw RequiredPageNotFound("starting pitcher statistic section not found")

    val categories = listOf("승패", "이닝", "평균 자책", "WHIP", "상대전적")
    val rows = startPitcherTblElement.select("tbody tr")

    assert(categories.size == rows.size) { "rows size should be ${categories.size}" }

    putJsonObject("away-team-pitcher") {
        for (i in rows.indices) {
            put(categories[i], rows[i].selectFirst("td:first-child .StartPlayer_score_area__2ENIv")!!.text())
        }
    }
    putJsonObject("home-team-pitcher") {
        for (i in rows.indices) {
            put(categories[i], rows[i].selectFirst("td:last-child .StartPlayer_score_area__2ENIv")!!.text())
        }
    }
}

internal fun NaverSportsBaseballMatchPage.extractStartingPitcherMostPitches(): JsonObject = buildJsonObject {
    val homeTeamMostPitchElement = this@extractStartingPitcherMostPitches.doc.select(".StartPlayer_chart_area__3Qvw_ .StartPlayer_chart__36izU").first()
        ?: throw RequiredPageNotFound("starting pitcher most pitches section not found")
    putJsonArray("away-team-picter-most-pitch") {
        homeTeamMostPitchElement.select(".StartPlayer_row__1wo7M")
            .take(3)
            .forEach { mostPitchElement ->
                addJsonObject {
                    put("구종", mostPitchElement.selectFirst(".StartPlayer_type__1LRwJ")!!.text())
                    put("구속", mostPitchElement.selectFirst(".StartPlayer_speed__3UlYQ")!!.text())
                    put("비율(퍼센트)", mostPitchElement.selectFirst(".StartPlayer_number__1SRt9")!!.text())
                }
            }
    }
    val awayTeamMostPitchElement = this@extractStartingPitcherMostPitches.doc.select(".StartPlayer_chart_area__3Qvw_ .StartPlayer_chart__36izU").last()
        ?: throw RequiredPageNotFound("starting pitcher most pitches section not found")
    putJsonArray("home-team-picter-most-pitch") {
        awayTeamMostPitchElement.select(".StartPlayer_row__1wo7M")
            .take(3)
            .forEach { mostPitchElement ->
                addJsonObject {
                    put("구종", mostPitchElement.selectFirst(".StartPlayer_type__1LRwJ")!!.text())
                    put("구속", mostPitchElement.selectFirst(".StartPlayer_speed__3UlYQ")!!.text())
                    put("비율(퍼센트)", mostPitchElement.selectFirst(".StartPlayer_number__1SRt9")!!.text())
                }
            }
    }
}