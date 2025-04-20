package io.gitp.llmarticlewrtier.spojoyscraper.extractor

import io.gitp.llmarticlewrtier.spojoyscraper.model.BaseballMatchListPage

/**
 * input은 아래와 같다
 * "javascript:openSpoDB(402775,'baseball','S','D');"
 * output은 402775(spojoy website의 mlb page id. 예시 https://spodb.spojoy.com/?game_id=402775 이런식)
 */
val extractMatchIdFromHref = Regex("""[0-9]+""")

fun BaseballMatchListPage.parseMlbMatchList(): List<String> {
    val aElements = this.doc.select("#ScheduleBox td #spodb_ a")
    return aElements
        .filter { aElement -> aElement.text() == "비교분석" }
        .map { aElement -> aElement.attribute("href")!!.value }
        .map { href -> extractMatchIdFromHref.find(href)!!.value }
        .map { mlbPageId -> "https://spodb.spojoy.com/?game_id=${mlbPageId}" }
}
