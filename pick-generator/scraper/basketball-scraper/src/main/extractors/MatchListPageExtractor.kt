package io.gitp.sbpick.pickgenerator.scraper.basketballscraper.extractors

import io.gitp.sbpick.pickgenerator.scraper.basketballscraper.models.BasketballMatchListPage
import java.net.URI

fun BasketballMatchListPage.extractMatchUrls(): List<URI> =
    this.doc
        .select(".event__match--withRowLink a.eventRowLink")
        .map { aElement -> aElement.attribute("href")!!.value }
        .map { URI(it) }
