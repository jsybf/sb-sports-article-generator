package io.gitp.sbpick.pickgenerator.scraper.basketballscraper.extractors

import io.gitp.sbpick.pickgenerator.scraper.basketballscraper.models.BasketballMatchListPage

fun BasketballMatchListPage.extractMatchUrls(): List<String> =
    this.doc
        .select(".event__match--withRowLink.event__match--scheduled a.eventRowLink")
        .map { aElement -> aElement.attribute("href")!!.value }
