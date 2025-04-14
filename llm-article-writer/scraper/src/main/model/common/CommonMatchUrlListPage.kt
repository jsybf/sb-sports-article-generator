package model.common

import org.jsoup.nodes.Document

data class CommonMatchUrlListPage(
    val doc: Document
) {
    fun extractMatchUrls(): List<String> =
        this.doc
            .select(".event__match--withRowLink a")
            .map { aElement -> aElement.attribute("href")!!.value }
}
