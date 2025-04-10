package examples

import scrape.PlaywrightBrowser
import scrape.hockey.HockeyMatchPages
import scrape.hockey.HockeyScraper
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

/**
 * request upcomming hockey match summary
 * then save response html file
 */
fun main() {
    val upcomingHockeyMatchPageList: List<HockeyMatchPages> = PlaywrightBrowser().use { browser ->
        HockeyScraper(browser).scrapeAllUpcomingMatchList()
    }

    val basePath = Path.of("test-data/scraped-html").toAbsolutePath()
    upcomingHockeyMatchPageList.forEachIndexed { idx, pages ->
        val saveDir = basePath.resolve("$idx").also { it.createDirectories() }
        saveDir.resolve("match.html").writeText(pages.matchSummaryPage.doc.html())
        pages.absencePlayerPageList.forEachIndexed { playerPageIdx, playerPage ->
            saveDir.resolve("player-${playerPageIdx}.html").writeText(playerPage.doc.html())
        }
    }
}