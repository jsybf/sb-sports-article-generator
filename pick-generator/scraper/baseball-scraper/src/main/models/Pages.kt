package io.gitp.sbpick.pickgenerator.scraper.baseballscraper.models

import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import org.jsoup.nodes.Document

internal data class BaseballMatchListPage(
    val doc: Document
)

internal data class StartingPitcerPage(
    val doc: Document
)

internal data class BaseballMatchPage(
    val doc: Document
)

internal data class BaseballPlayerListPage(
    val doc: Document
)

internal data class BaseballPlayerPage(
    val doc: Document
)

internal data class NaverSportsBaseballMatchListPage(
    val doc: Document,
    val league: League.Baseball
)

internal data class NaverSportsBaseballMatchPage(
    val doc: Document
)
