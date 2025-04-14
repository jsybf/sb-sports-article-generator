import model.Leaguee
import model.MatchInfo
import scrape.BasketballScraper

class BasketBallScrapeSrevice(
    browser: PlaywrightBrowser
) {
    private val baseketBallScraper = BasketballScraper(browser)

    fun scrapeUpcommingMatch(league: Leaguee.BasketBall): Sequence<MatchInfo> =
        baseketBallScraper
            .requestUpcommingMatchListPage(league)
            .extractMatchUrls()
            .asSequence()
            .map { url ->
                // 스크래핑시 배당률 관련 버튼들이 아직 존재하지 않으면 playwright가 오류를 뱉음. 이오류는 넘겨야함.
                runCatching {
                    val matchPage = baseketBallScraper.requestMatchPage(url)
                    val oneXTwoBetOdds = baseketBallScraper.requestOneXTwoBetPage(url).extractOdds().also { odds -> if (odds.size == 0) return@map null }
                    val overUnderBetOdds = baseketBallScraper.requestOverUnderBetPage(url).extractOdds().also { odds -> if (odds.size == 0) return@map null }

                    val (homeTeam, awayTeam) = matchPage.extractTeams()
                    MatchInfo(
                        awayTeam = awayTeam,
                        homeTeam = homeTeam,
                        matchAt = matchPage.extractStartAt(),
                        league = league,
                        matchPageUrl = url,
                        matchSummary = matchPage.extractMatchInfo(),
                        oneXTwoBet = oneXTwoBetOdds,
                        overUnderBet = overUnderBetOdds,
                    )
                }.getOrNull()
            }
            .filterNotNull()
}

// example
private fun main() {
    PlaywrightBrowser().use { browser ->
        BasketBallScrapeSrevice(browser).scrapeUpcommingMatch(Leaguee.BasketBall.CBA).forEach { println(it) }
    }
}
