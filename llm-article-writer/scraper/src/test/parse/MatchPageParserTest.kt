package parse

import kotlinx.serialization.json.Json
import model.HockeyPage
import org.jsoup.Jsoup
import org.junit.Test
import readResourceFile

class MatchPageParserTest {
    private val sampleHtml = readResourceFile("/match-page-sample.html").let { Jsoup.parse(it) }

    // 단순 출력 확인용
    @Test
    fun `parse odds test`() {
        HockeyPage.MatchPage(sampleHtml).parse()
            .let { Json { prettyPrint = true }.encodeToString(it) }
            .also { println(it) }
    }
}