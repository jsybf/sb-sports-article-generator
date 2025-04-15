package io.gitp.llmarticlewriter.scraper.service

import io.gitp.llmarticlewriter.scraper.PlaywrightBrowser
import io.gitp.llmarticlewriter.scraper.model.League
import io.gitp.llmarticlewriter.scraper.model.MatchInfo
import io.gitp.llmarticlewriter.scraper.scrape.HockeyScraper


class HockeyScrapeService(
    browser: PlaywrightBrowser
) {
    private val hockeyScraper = HockeyScraper(browser)

    fun scrapeUpcommingMatch(league: League.Hockey): Sequence<MatchInfo> = hockeyScraper
        .requestUpcommingMatchListPage(league)
        .extractMatchUrls()
        .asSequence()
        .map { url ->
            // 스크래핑시 배당률 관련 버튼들이 아직 존재하지 않으면 playwright가 오류를 뱉음. 이오류는 넘겨야함.
            runCatching {
                val matchPage = hockeyScraper.requestMatchPage(url)
                val oneXTwoBetPage = hockeyScraper.requestOneXTwoBetPage(url).extractOdds().also { odds -> if (odds.size == 0) return@map null }
                val overUnderBetPage = hockeyScraper.requestOverUnderBetPage(url).extractOdds().also { odds -> if (odds.size == 0) return@map null }

                val (homeTeam, awayTeam) = matchPage.extractTeams()
                MatchInfo(
                    awayTeam = awayTeam,
                    homeTeam = homeTeam,
                    matchAt = matchPage.extractStartAt(),
                    league = league,
                    matchPageUrl = url,
                    matchSummary = matchPage.extractMatchInfo(),
                    oneXTwoBet = oneXTwoBetPage,
                    overUnderBet = overUnderBetPage
                )
            }.getOrNull()
        }
        .filterNotNull()
}