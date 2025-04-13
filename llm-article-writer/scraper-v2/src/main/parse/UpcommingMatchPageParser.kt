package parse

import model.HockeyPage

internal fun HockeyPage.UpcommingMatcListhPage.parseMatchUrl(): List<String> =
    this.doc
        .select(".event__match--withRowLink a")
        .map { aElement -> aElement.attribute("href")!!.value }