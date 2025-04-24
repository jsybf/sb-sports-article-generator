package io.gitp.sbpick.pickgenerator.scraper.scrapebase.models

import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool

interface ScrapePipeline<out L : League> {
    /**
     * 예정경기들의 url 스크래핑
     */
    suspend fun scrapeFixtureUrls(browserPool: PlaywrightBrowserPool, league: @UnsafeVariance L): List<String>

    /**
     * 예정경기 상세 스크래핑
     */
    suspend fun scrapeMatch(browserPool: PlaywrightBrowserPool, league: @UnsafeVariance L, matchUrl: String): Result<Pair<MatchInfo, LLMAttachment>>
}