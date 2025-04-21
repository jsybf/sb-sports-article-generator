package io.gitp.sbpick.pickgenerator.scraper.baseballscraper.extractors

import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.models.BaseballMatchListPage
import java.net.URI

/**
 * input은 아래와 같다
 * "javascript:openSpoDB(402775,'baseball','S','D');"
 * output은 402775(spojoy website의 mlb page id. 예시 https://spodb.spojoy.com/?game_id=402775 이런식)
 */
private val extractMatchIdFromHref = Regex("""[0-9]+""")

internal fun BaseballMatchListPage.parseMlbMatchList(): List<URI> {
    val aElements = this.doc.select("#ScheduleBox td #spodb_ a")
    return aElements
        .filter { aElement -> aElement.text() == "비교분석" }
        .map { aElement -> aElement.attribute("href")!!.value }
        .map { href -> extractMatchIdFromHref.find(href)!!.value }
        .map { mlbPageId -> "https://spodb.spojoy.com/?game_id=${mlbPageId}" }
        .map { URI(it) }

}
