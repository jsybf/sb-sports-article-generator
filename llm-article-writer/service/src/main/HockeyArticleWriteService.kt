package io.gitp.llmarticlewriter.service

import io.gitp.llmarticlewriter.database.repo.HockeyRepo
import io.gitp.llmarticlewriter.llmwriter.ClaudeSportArticleWriter
import scrape.PlaywrightBrowser
import scrape.hockey.HockeyPage
import scrape.hockey.HockeyScraper
import scrape.hockey.parse.parseStartDateTime
import scrape.hockey.parse.parseTeam

class HockeyArticleWriteService(
    private val hockeyRepo: HockeyRepo,
    private val llmArticleWriter: ClaudeSportArticleWriter
) {
    /**
     * 소스 사이트에서 현제 디비에 있지 않은 예정경기 정보들을 스크래핑
     * @return 디비에 삽입된 [HockeyPage.MatchPageSet]들
     */
    fun scrape(): List<HockeyPage.MatchPageSet> = PlaywrightBrowser().use { browser ->
        val scraper = HockeyScraper(browser)
        val upcommingMatchPageSet: List<HockeyPage.MatchPageSet> = scraper.scrapeAllUpcomingMatchList()

        upcommingMatchPageSet
            .filter { matchPageSet -> !hockeyRepo.ifMatchExist(matchPageSet.matchSummaryPage) }
            .onEach { matchPageSet ->
                val (homeTeam, awayTeam) = matchPageSet.matchSummaryPage.parseTeam()
                val startAt = matchPageSet.matchSummaryPage.parseStartDateTime()
                println("[INFO] inserting hockey match (homeTeam:${homeTeam})(awayTeam:${awayTeam})(startAt:${startAt})")
                hockeyRepo.insertMatchPageSetAndGetId(matchPageSet)
            }
    }

    /**
     * 디비에 있는 하키 경기들중 픽(추천글)이 없는 것들의 추천글 생성
     * @return 삽입된 article들의 pk
     */
    fun generateArticle() {
        hockeyRepo
            .findMatchPageSetNotHavingArticle()
            .map { matchPageSetDto ->
                val article = llmArticleWriter.generateArticle(matchPageSetDto.matchPageSet.toLLMQueryAttachment())
                hockeyRepo.insertLLMArticleAndGetId(matchPageSetDto.id, article)
            }
    }
}