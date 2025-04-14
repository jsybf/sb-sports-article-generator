package parse

import io.gitp.llmarticlewriter.scraper.model.pages.common.CommonOverUnderBetPage
import io.gitp.llmarticlewriter.scraper.model.pages.common.CommonOverUnderBetPageParser.parseOdds
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.junit.Test
import readResourceFile

class OverUnderBetPageParseTest {
    private val sampleHtml = readResourceFile("/over-under-odds-page-sample.html").let { Jsoup.parse(it) }

    // 단순 출력 확인용
    @Test
    fun `parse odds test`() {
        CommonOverUnderBetPage(sampleHtml).parseOdds()
            .let { Json { prettyPrint = true }.encodeToString(it) }
            .also { println(it) }
    }
}