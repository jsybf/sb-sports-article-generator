package io.gitp.sbpick.pickgenerator.scraper.scrapebase.models

import java.net.URL

interface ScapePipeline<T : MatchInfo> {
    fun getFixtureUrl(): List<URL>
    fun scrape(urlList: List<URL>): Sequence<MatchInfo>
}