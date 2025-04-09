import org.jsoup.nodes.Element

fun main(): Unit {
    val browser = PlaywrightBrowser()

    val attachData =  HockeyScraper
        .upcommingMatchSummaryUrlList(browser)
        .first()
        .let { sampleUrl ->
            scrapeHockeyArticleAttachData(
                browser,
                sampleUrl
            )
        }

    attachData.toLLMQueryString().also { println(it) }
    browser.close()
}
