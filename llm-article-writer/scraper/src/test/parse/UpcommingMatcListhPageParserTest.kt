package parse

import io.gitp.llmarticlewriter.scraper.model.pages.common.CommonMatchUrlListPage
import org.jsoup.Jsoup
import org.junit.Test
import kotlin.test.assertEquals

class UpcommingMatcListhPageParserTest {
    private fun readResourceFile(path: String): String = object {}::class.java.getResource(path)!!.readText()

    private val sampleHtml = readResourceFile("/match-list-page-sample.html").let { Jsoup.parse(it) }

    @Test
    fun `parse match page url link from match list page`() {
        assertEquals(
            CommonMatchUrlListPage(sampleHtml).extractMatchUrls(),
            listOf(
                "https://www.flashscore.co.kr/match/hockey/2aZjIqZI/#/match-summary",
                "https://www.flashscore.co.kr/match/hockey/GGbd7heD/#/match-summary",
                "https://www.flashscore.co.kr/match/hockey/x6mcbcCr/#/match-summary",
                "https://www.flashscore.co.kr/match/hockey/SjQ8GdYQ/#/match-summary",
                "https://www.flashscore.co.kr/match/hockey/8AorVemK/#/match-summary",
                "https://www.flashscore.co.kr/match/hockey/U5RxtmRP/#/match-summary",
                "https://www.flashscore.co.kr/match/hockey/YXt6dyse/#/match-summary",
                "https://www.flashscore.co.kr/match/hockey/CO1uBzdl/#/match-summary",
                "https://www.flashscore.co.kr/match/hockey/jVl7PZAs/#/match-summary",
                "https://www.flashscore.co.kr/match/hockey/nN79zRlm/#/match-summary",
                "https://www.flashscore.co.kr/match/hockey/lA8WnX5e/#/match-summary"
            )
        )
    }
}