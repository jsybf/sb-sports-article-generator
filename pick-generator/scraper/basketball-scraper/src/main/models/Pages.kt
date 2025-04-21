package io.gitp.sbpick.pickgenerator.scraper.basketballscraper.models

import org.jsoup.nodes.Document

data class BasketballMatchListPage(
    val doc: Document
)

internal data class BasketballMatchPage(
    val doc: Document
)

internal data class OneXTwoBetPage(
    val doc: Document
)

internal class OverUnderBetPage(
    val doc: Document
)
