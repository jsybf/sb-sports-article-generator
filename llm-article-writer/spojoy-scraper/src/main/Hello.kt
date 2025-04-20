package io.gitp.llmarticlewrtier.spojoyscraper

import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory


private val logger = LoggerFactory.getLogger(object {}::class.java.packageName)

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

private fun main() {
    PlaywrightBrowser().use { browser ->
        val doc = browser.doAndGetDoc {
            navigate("https://www.spojoy.com/live/?mct=baseball#rs")
        }
        println(doc)
    }
}
