package scrape.hockey

import kotlinx.serialization.json.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.readText

fun main() {
    // HTML 파일 로드 (또는 문자열에서 직접 파싱할 수도 있음)
    val doc = Path.of("test-data/match.html").readText().let { Jsoup.parse(it) }

    // JSON 빌드
    val matchJson = buildMatchJson(doc)

    // 결과 출력
    Json { prettyPrint = true }.encodeToString(matchJson).let { println(it) }
}

private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
fun buildMatchJson(doc: Document): JsonObject {
    return buildJsonObject {
        // Match 정보 추출
        putJsonObject("match") {
            val dateTime = doc.selectFirst(".duelParticipant__startTime div")!!.text().let { LocalDateTime.parse(it, formatter) }
            put(
                "start_datetime",
                dateTime.toString()
            )

            put(
                "league",
                doc.selectFirst("#detail > div.detail__breadcrumbs  a > span:last-of-type")!!.text()
            )
        }
        put(
            "ranks",
            buildJsonArray {
                doc.select(".ui-table__row").map { rowElement ->
                    buildJsonObject {
                        put(
                            "rank",
                            rowElement.selectFirst(".tableCellRank")!!.text()
                        )
                        rowElement.select(".table__cell.table__cell--value")!!
                            .also { assert(it.size == 7) }
                            .let { col ->
                                put("match_cnt", col[0].text())
                                put("win_cnt", col[1].text())
                                put("dual_win_cnt", col[2].text())
                                put("lose", col[4].text())
                                put("win-lose", col[5].text())
                                put("point", col[6].text())
                            }
                    }
                }
                    .forEach { add(it) }
            }
        )


        // Teams 정보 추출
        putJsonObject("teams") {

            putJsonObject("home") {
                put("name", doc.select(".duelParticipant__home .participant__participantName").text())
                put("rank", doc.select(".detailTeamForm__team--home .wcl-badgeInfo_cdKy0").text().replace(".", ""))
                put("group", doc.select(".ui-table__headerCell.table__headerCell--participant").attr("title"))

                // Standing 정보
                putJsonObject("standing") {
                    val homeTeamRow = doc.select(".ui-table__row").find {
                        it.select(".tableCellParticipant__name").text() == "노르웨이 여"
                    }

                    homeTeamRow?.let {
                        put("played", it.select(".table__cell.table__cell--value").get(0).text().toInt())
                        put("wins", it.select(".table__cell.table__cell--value").get(1).text().toInt())
                        put("losses", it.select(".table__cell.table__cell--value").get(4).text().toInt())
                        put("points", it.select(".table__cell.table__cell--value.table__cell--points").text().toInt())
                        put("goals", it.select(".table__cell.table__cell--value.table__cell--score").text())
                    }
                }

                // Form 정보
                putJsonArray("form") {
                    doc.select(".detailTeamForm__team--home .wcl-badgeform_yYFgV span").forEach {
                        add(it.text())
                    }
                }
            }

            putJsonObject("away") {
                put("name", doc.select(".duelParticipant__away .participant__participantName").text())
                put("rank", doc.select(".detailTeamForm__team--away .wcl-badgeInfo_cdKy0").text().replace(".", ""))
                put("group", doc.select(".ui-table__headerCell.table__headerCell--participant").attr("title"))

                // Standing 정보
                putJsonObject("standing") {
                    val awayTeamRow = doc.select(".ui-table__row").find {
                        it.select(".tableCellParticipant__name").text() == "독일 여"
                    }

                    awayTeamRow?.let {
                        put("played", it.select(".table__cell.table__cell--value").get(0).text().toInt())
                        put("wins", it.select(".table__cell.table__cell--value").get(1).text().toInt())
                        put("losses", it.select(".table__cell.table__cell--value").get(4).text().toInt())
                        put("points", it.select(".table__cell.table__cell--value.table__cell--points").text().toInt())
                        put("goals", it.select(".table__cell.table__cell--value.table__cell--score").text())
                    }
                }

                // Form 정보
                putJsonArray("form") {
                    doc.select(".detailTeamForm__team--away .wcl-badgeform_yYFgV span").forEach {
                        add(it.text())
                    }
                }
            }
        }

        // Head to Head 정보 추출
        putJsonArray("headToHead") {
            doc.select(".h2h__row").forEach { row ->
                addJsonObject {
                    put("date", row.select(".h2h__date").text())
                    put("tournament", row.select(".h2h__event").attr("title"))
                    put("home", row.select(".h2h__homeParticipant .h2h__participantInner").text())
                    put("away", row.select(".h2h__awayParticipant .h2h__participantInner").text())

                    val scores = row.select(".h2h__result span")
                    if (scores.size >= 2) {
                        put("score", "${scores[0].text()}-${scores[1].text()}")
                    }

                    val regularTimeScores = row.select(".h2h__regularTimeResult")
                    if (regularTimeScores.size >= 2 && !regularTimeScores[0].text().isEmpty()) {
                        put("regularTime", "${regularTimeScores[0].text()}-${regularTimeScores[1].text()}")
                    }
                }
            }
        }
    }
}