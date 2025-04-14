package parse

import kotlinx.serialization.json.*
import model.HockeyPage

// llm 이 작성...
internal fun HockeyPage.OneXTwoBetPage.parseOdds(): JsonArray = buildJsonArray {
    val rows = doc.select(".ui-table__row")
    for (row in rows) {
        val bookmakerName = row.select(".oddsCell__bookmaker img").attr("title")
        if (bookmakerName.isEmpty()) continue

        // 배당률 값 추출
        val odds = row.select(".oddsCell__odd span")
        val oddValues = mutableListOf<String>()

        for (odd in odds) {
            oddValues.add(odd.text())
        }

        // 이전 배당률 추출
        val previousOdds = row.select(".oddsCell__odd")
        val previousOddValues = mutableListOf<String>()
        val trends = mutableListOf<String>()

        for (odd in previousOdds) {
            val title = odd.attr("title")
            if (title.isNotEmpty() && title.contains("»")) {
                val parts = title.split("»")
                val previousValue = parts[0].trim()
                previousOddValues.add(previousValue)

                // 추세 확인
                val hasDownArrow = odd.select(".arrowDown-ico").isNotEmpty()
                val hasUpArrow = odd.select(".arrowUp-ico").isNotEmpty()
                if (hasDownArrow) {
                    trends.add("down")
                } else if (hasUpArrow) {
                    trends.add("up")
                } else {
                    trends.add("unchanged")
                }
            } else {
                previousOddValues.add("")
                trends.add("")
            }
        }

        // 북메이커 배당 정보 JSON 객체 추가
        addJsonObject {
            put("bookmaker", JsonPrimitive(bookmakerName))

            // 현재 배당률
            putJsonObject("odds") {
                if (oddValues.size >= 1 && oddValues[0] != "-") put("1", JsonPrimitive(oddValues[0]))
                else put("1", JsonPrimitive("-"))

                if (oddValues.size >= 2 && oddValues[1] != "-") put("X", JsonPrimitive(oddValues[1]))
                else put("X", JsonPrimitive("-"))

                if (oddValues.size >= 3 && oddValues[2] != "-") put("2", JsonPrimitive(oddValues[2]))
                else put("2", JsonPrimitive("-"))
            }

            // 이전 배당률이 있는 경우에만 추가
            if (previousOddValues.any { it.isNotEmpty() }) {
                putJsonObject("previous_odds") {
                    if (previousOddValues.size >= 1 && previousOddValues[0].isNotEmpty())
                        put("1", JsonPrimitive(previousOddValues[0]))

                    if (previousOddValues.size >= 2 && previousOddValues[1].isNotEmpty())
                        put("X", JsonPrimitive(previousOddValues[1]))

                    if (previousOddValues.size >= 3 && previousOddValues[2].isNotEmpty())
                        put("2", JsonPrimitive(previousOddValues[2]))
                }

                putJsonObject("trend") {
                    if (trends.size >= 1 && trends[0].isNotEmpty())
                        put("1", JsonPrimitive(trends[0]))

                    if (trends.size >= 2 && trends[1].isNotEmpty())
                        put("X", JsonPrimitive(trends[1]))

                    if (trends.size >= 3 && trends[2].isNotEmpty())
                        put("2", JsonPrimitive(trends[2]))
                }
            }
        }
    }
}