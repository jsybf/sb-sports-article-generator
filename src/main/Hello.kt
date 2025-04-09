import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

fun main(args: Array<String>): Unit {
    val broswer = PlaywrightBrowser()

    val links = broswer
        .doAndGet {
            navigate("https://www.flashscore.co.kr/hockey/")
            locator("#live-table > div.filters > div.filters__group > div:nth-child(5) > div").click()
        }
        .select("section.event div.event__match > a")
        .map { it.attribute("href")?.value }
        .filterNotNull()
        .onEach { println(it) }

    broswer.close()
    println("end")
}

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
