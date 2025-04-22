package io.gitp.sbpick.pickgenerator.scraper.hockeyscraper.extractors

import io.gitp.sbpick.pickgenerator.scraper.hockeyscraper.models.HockeyMatchListPage

fun HockeyMatchListPage.extractMatchUrls(): List<String> =
    this.doc
        .select(".event__match--withRowLink.event__match--scheduled a.eventRowLink")
        .map { aElement -> aElement.attribute("href")!!.value }
