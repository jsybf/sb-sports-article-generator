package io.gitp.llmarticlewriter.scraper

import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class PlaywrightBrowser : AutoCloseable {
    private val playwright: Playwright = Playwright.create()
    private val browser = playwright.chromium().launch()

    fun doAndGetDoc(actions: Page.() -> Unit): Document {
        val page = browser.newPage()
        page.actions()
        Playwright.create().use { }

        val html = page.content().let { Jsoup.parse(it) }
        page.close()
        return html
    }

    override fun close() {
        browser.close()
        playwright.close()
    }
}
