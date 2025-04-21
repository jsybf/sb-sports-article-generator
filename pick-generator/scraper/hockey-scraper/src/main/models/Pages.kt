package io.gitp.sbpick.pickgenerator.scraper.hockeyscraper.models

import org.jsoup.nodes.Document

data class HockeyMatchListPage(
    val doc: Document
)

internal data class HockeyMatchPage(
    val doc: Document
)

internal data class OneXTwoBetPage(
    val doc: Document
)

internal class OverUnderBetPage(
    val doc: Document
)
