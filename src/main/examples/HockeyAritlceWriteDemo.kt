package examples

import scrape.PlaywrightBrowser
import scrape.hockey.HockeyMatchPages
import scrape.hockey.HockeyScraper
import writer.ClaudeSportArticleWriter

fun main() {
    val upcomingHockeyMatchPagesList: List<HockeyMatchPages> = PlaywrightBrowser().use { browser ->
        HockeyScraper(browser).scrapeAllUpcomingMatchList()
    }

    val writer = (System.getenv("CLAUDE_API_KEY") ?: throw IllegalStateException("env CLAUDE_API_KEY deosn't exist"))
        .let { apikey -> ClaudeSportArticleWriter(apiKey = apikey) }

    upcomingHockeyMatchPagesList.first()
        .let { pages -> writer.generateArticle(pages.toLLMQueryAttachment()) }
}