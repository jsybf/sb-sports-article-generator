import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class PlaywrightBrowser {
    private val playwright: Playwright = Playwright.create()
    private val browser = playwright.chromium().launch()

    fun doAndGet(actions: Page.() -> Unit): Document {
        val page = browser.newPage()
        page.actions()

        val html = page.content().let { Jsoup.parse(it) }
        page.close()
        return html
    }

    fun close() {
        browser.close()
        playwright.close()
    }
}

fun scrapeHockeyArticleAttachData(browser: PlaywrightBrowser, matchPageUrl: String): HockeyArticleAttachData {
    val hockeyMatchSummaryDoc: HockeyPage.SummaryPage= HockeyScraper.matchSummaryDoc(browser, matchPageUrl)


    val absencePlayerDocList: List<HockeyPage.PlayerPage> =  hockeyMatchSummaryDoc.parsePlayerUrlList().map { url-> HockeyScraper.playerDoc(browser, url) }

    return HockeyArticleAttachData(
        matchSummaryHtml = hockeyMatchSummaryDoc,
        absencePlayerList = absencePlayerDocList
    )
}

object HockeyScraper {
    fun upcommingMatchSummaryUrlList(browser: PlaywrightBrowser): List<String> {
        return browser
            .doAndGet {
                navigate("https://www.flashscore.co.kr/hockey/")
                locator("#live-table > div.filters > div.filters__group > div:nth-child(5) > div").click()
            }
            .select("section.event div.event__match > a")
            .map { it.attribute("href")?.value }
            .filterNotNull()

    }

    fun matchSummaryDoc(broswer: PlaywrightBrowser, url: String): HockeyPage.SummaryPage{
        return broswer
            .doAndGet {
            println("[INFO] requesting hockey-match-summary ($url)")
            navigate(url)
            this.locator(".pending").all().forEach { assertThat(it).isHidden() }
        }
            .let { HockeyPage.SummaryPage(it) }
    }

    fun playerDoc(broswer: PlaywrightBrowser, url: String): HockeyPage.PlayerPage{
        return broswer.doAndGet {
            println("[INFO] requesting hockey-player ($url)")
            navigate(url)
        }
            .let { HockeyPage.PlayerPage(it) }
    }
}



fun main(): Unit {
    val browser = PlaywrightBrowser()

    val upCommingHockeyMatchLinks =  HockeyScraper.upcommingMatchSummaryUrlList(browser)
    val matchSampleUrl = upCommingHockeyMatchLinks.first()

    scrapeHockeyArticleAttachData(browser, matchSampleUrl)
    browser.close()
    println("end")
}
