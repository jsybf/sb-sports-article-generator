package parse

import io.gitp.llmarticlewriter.scraper.model.pages.common.CommonOneXTwoBetPage
import io.gitp.llmarticlewriter.scraper.model.pages.common.CommonOneXTwoBetPageParser.parseOdds
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.junit.Test
import readResourceFile

class OneXTwoBetPageParserTest {
    private val sampleHtml = readResourceFile("/1x2-odds-page-sample.html").let { Jsoup.parse(it) }

    // 단순 출력 확인용
    @Test
    fun `parse odds test`() {
        CommonOneXTwoBetPage(sampleHtml).parseOdds()
            .let { Json { prettyPrint = true }.encodeToString(it) }
            .also { println(it) }
    }
}