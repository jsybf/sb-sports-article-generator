package parse

import io.gitp.llmarticlewriter.scraper.model.pages.hockey.HockeyMatchPage
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.junit.Test
import readResourceFile

class BasketBallMatchPageParserTest {
    private val sampleHtml = readResourceFile("/match-page-sample.html").let { Jsoup.parse(it) }

    // 단순 출력 확인용
    @Test
    fun `parse odds test`() {
        HockeyMatchPage(sampleHtml).extractMatchInfo()
            .let { Json { prettyPrint = true }.encodeToString(it) }
            .also { println(it) }
    }
}