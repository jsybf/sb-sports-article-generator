package io.gitp.llmarticlewrtier.spojoyscraper.extractor

import io.gitp.llmarticlewrtier.spojoyscraper.PlaywrightWorkerPool
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory
import java.util.concurrent.Future

/**
 * input은 아래와 같다
 * "javascript:openSpoDB(402775,'baseball','S','D');"
 * output은 402775(spojoy website의 mlb page id. 예시 https://spodb.spojoy.com/?game_id=402775 이런식)
 */
val extractMatchIdFromHref = Regex("""[0-9]+""")

fun Document.parseMlbMatchList(): List<String> {
    val aElements = this.select("#ScheduleBox td #spodb_ a")
    return aElements
        .filter { aElement -> aElement.text() == "비교분석" }
        .map { aElement -> aElement.attribute("href")!!.value }
        .map { href -> extractMatchIdFromHref.find(href)!!.value }
        .map { mlbPageId -> "https://spodb.spojoy.com/?game_id=${mlbPageId}" }
}

private val logger = LoggerFactory.getLogger(object {}::class.java.packageName)
private fun main() {
    PlaywrightWorkerPool(1).use { browserWorkerPool ->
        val mlbMatchListPage: Future<Document> = browserWorkerPool.submitTask {
            logger.info("requesting1 spojoy mlb match list page(https://www.spojoy.com/live/?mct=baseball#rs)")
            navigate("https://www.spojoy.com/live/?mct=baseball#rs")
        }


        mlbMatchListPage.get().parseMlbMatchList().forEach { println(it) }
        // println("${doc1.get()}")
    }
}
