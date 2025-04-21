package io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser

import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class PlaywrightBrowser : AutoCloseable {
    private val playwright: Playwright = Playwright.create()
    private val browser = playwright.chromium().launch()

    suspend fun doAndGetDoc(actions: suspend Page.() -> Unit): Document {
        val page = browser.newPage()

        page.actions()
        val html = page.content().let { Jsoup.parse(it) }

        page.close()

        return html
    }

    override fun close() {
        browser.close()
        playwright.close()
    }
}
