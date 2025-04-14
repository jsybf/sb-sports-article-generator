package parse

import kotlinx.serialization.json.*
import model.HockeyPage

// llm 이 작성...
internal fun HockeyPage.OverUnderBetPage.parseOverUnderOdds() = buildJsonArray {
    val tables = doc.select(".ui-table.oddsCell__odds")
    for (table in tables) {
        // 각 테이블에서 총점 추출
        val total = table.selectFirst(".wcl-oddsValue_Fc9sZ")!!.text()
        // 각 북메이커의 배당률 추출
        val rows = table.select(".ui-table__row")
        addJsonObject {
            put("total", JsonPrimitive(total))
            putJsonArray("bookmakers") {
                for (row in rows) {
                    val bookmakerName = row.select(".oddsCell__bookmaker img").attr("title")
                    if (bookmakerName.isEmpty()) continue

                    // 오버 배당률
                    val overOddSpan = row.select(".oddsCell__odd").first()?.select("span")?.not(".oddsCell__lineThrough")
                    val overOdd = overOddSpan?.text() ?: "-"
                    val isOverOddLineThrough = row.select(".oddsCell__odd").first()?.select(".oddsCell__lineThrough")?.isNotEmpty() ?: false

                    // 언더 배당률
                    val underOddSpan = row.select(".oddsCell__odd").last()?.select("span")?.not(".oddsCell__lineThrough")
                    val underOdd = underOddSpan?.text() ?: "-"
                    val isUnderOddLineThrough = row.select(".oddsCell__odd").last()?.select(".oddsCell__lineThrough")?.isNotEmpty() ?: false

                    // 추세 확인
                    val overHasUpArrow = row.select(".oddsCell__odd").first()?.select(".arrowUp-ico")?.isNotEmpty() ?: false
                    val overHasDownArrow = row.select(".oddsCell__odd").first()?.select(".arrowDown-ico")?.isNotEmpty() ?: false
                    val underHasUpArrow = row.select(".oddsCell__odd").last()?.select(".arrowUp-ico")?.isNotEmpty() ?: false
                    val underHasDownArrow = row.select(".oddsCell__odd").last()?.select(".arrowDown-ico")?.isNotEmpty() ?: false

                    // 이전 배당률 추출 (title 속성에서)
                    val overTitle = row.select(".oddsCell__odd").first()?.attr("title") ?: ""
                    val underTitle = row.select(".oddsCell__odd").last()?.attr("title") ?: ""

                    val overPreviousOdd = if (overTitle.contains("»")) {
                        overTitle.split("»")[0].trim()
                    } else {
                        ""
                    }

                    val underPreviousOdd = if (underTitle.contains("»")) {
                        underTitle.split("»")[0].trim()
                    } else {
                        ""
                    }

                    // 배당률이 "-"이거나 취소선이 그어진 경우를 필터링
                    val validOverOdd = overOdd != "-" && !isOverOddLineThrough
                    val validUnderOdd = underOdd != "-" && !isUnderOddLineThrough

                    // 두 배당률 모두 유효하지 않으면 이 북메이커는 건너뜀
                    if (!validOverOdd && !validUnderOdd) continue

                    addJsonObject {
                        put("bookmaker", JsonPrimitive(bookmakerName))

                        // 유효한 오버 배당률만 포함
                        if (validOverOdd) {
                            putJsonObject("over") {
                                put("odd", JsonPrimitive(overOdd))

                                if (overPreviousOdd.isNotEmpty()) {
                                    put("previous_odd", JsonPrimitive(overPreviousOdd))
                                }

                                if (overHasUpArrow) {
                                    put("trend", JsonPrimitive("up"))
                                } else if (overHasDownArrow) {
                                    put("trend", JsonPrimitive("down"))
                                }
                            }
                        }

                        // 유효한 언더 배당률만 포함
                        if (validUnderOdd) {
                            putJsonObject("under") {
                                put("odd", JsonPrimitive(underOdd))

                                if (underPreviousOdd.isNotEmpty()) {
                                    put("previous_odd", JsonPrimitive(underPreviousOdd))
                                }

                                if (underHasUpArrow) {
                                    put("trend", JsonPrimitive("up"))
                                } else if (underHasDownArrow) {
                                    put("trend", JsonPrimitive("down"))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
