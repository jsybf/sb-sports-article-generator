package io.gitp.llmarticlewrtier.spojoyscraper.model

import org.jsoup.nodes.Document

data class BaseballMatchListPage(
    val doc: Document
)

data class StartingPitcerPage(
    val doc: Document
)

data class BaseballMatchPage(
    val doc: Document
)

data class BaseballPlayerListPage(
    val doc: Document
)

data class BaseballPlayerPage(
    val doc: Document
)