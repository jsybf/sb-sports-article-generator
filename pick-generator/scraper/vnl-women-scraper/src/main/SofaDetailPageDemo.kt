package io.gitp.sbpick.pickgenerator.scraper.vnlwommenscraper

import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.vnlwommenscraper.pages.SofaDetailPage


suspend fun main() {
    val browserPool = PlaywrightBrowserPool(1)

    val detailPage = browserPool.doAndGetDoc {
        navigate("https://www.sofascore.com/volleyball/match/turkiye-france/dJcsFJc#id:13315035")
        repeat(10) { this.mouse().wheel(0.0, 100.0) } // 마우스 스크롤을 해야지 통계 정보드이 로딩이 된다...
    }.let { SofaDetailPage(it) }

    //TODO log


    browserPool.close()
}