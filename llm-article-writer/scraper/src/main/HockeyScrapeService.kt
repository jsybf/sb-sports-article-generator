import model.HockeyMatchInfo
import model.League
import parse.*
import scrape.HockeyScraper

/**
 * main entry point of this maven module
 */
public class HockeyScrapeService(
    browser: PlaywrightBrowser
) {
    private val hockeyScraper = HockeyScraper(browser)

    fun scrapeUpcommingMatch(league: League): List<HockeyMatchInfo> = hockeyScraper
        .requestUpcommingMatchListPage(league)
        .parseMatchUrl()
        .map { url ->
            val mapPage = hockeyScraper.requestMatchPage(url)
            val oneXTwoBetPage = hockeyScraper.requestOneXTwoBetPage(url)
            val overUnderBetPage = hockeyScraper.requestOverUnderBetPage(url)

            val (homeTeam, awayTeam) = mapPage.parseTeam()
            HockeyMatchInfo(
                awayTeam = awayTeam,
                homeTeam = homeTeam,
                matchAt = mapPage.parseStartDateTime(),
                league = league,
                matchPageUrl = url,
                matchSummary = mapPage.parse(),
                oneXTwoBet = oneXTwoBetPage.parseOdds(),
                overUnderBet = overUnderBetPage.parseOverUnderOdds()
            )
        }
}

// example
private fun main() {
    PlaywrightBrowser().use { browser ->
        HockeyScrapeService(browser).scrapeUpcommingMatch(League.KHL)
    }
}
