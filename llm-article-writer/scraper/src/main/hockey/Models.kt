package scrape.hockey

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import scrape.LLMQueryAttachment
import scrape.SportsWebPage

object HockeyPage {
    data class SummaryPage(val doc: Document) : SportsWebPage {
        override fun extractMeaningful(): Element = listOf(
            doc.select("div.container__livetable div.duelParticipant"),
            doc.select("div.container__livetable div.loadable.complete")
        )
            .flatten()
            .fold(Element("div"), { acc, e -> acc.appendChild(e) })
    }

    data class PlayerPage(val doc: Document) : SportsWebPage {
        override fun extractMeaningful(): Element = listOf(
            doc.select("div.container__livetable.singlePageApp > #player-profile"),
            doc.select("div.container__livetable.singlePageApp > div.playerHeader__wrapper")
        )
            .flatten()
            .fold(Element("div"), { acc, e -> acc.appendChild(e) })
    }

    data class MatchPageSet(
        val matchSummaryPage: HockeyPage.SummaryPage,
        val absencePlayerPageList: List<HockeyPage.PlayerPage>
    ) : LLMQueryAttachment {
        override fun toLLMQueryAttachment(): String {
            return buildString {
                append("---이 부분아래는 예정된 하키경기의 html 일부분이야\n")
                append(matchSummaryPage.extractMeaningful().srinkHtml().html())

                absencePlayerPageList.forEach { playerPage: HockeyPage.PlayerPage ->
                    append("---이 부분아래는 결장하는 선수의 html 일부분이야\n")
                    append(playerPage.extractMeaningful().srinkHtml().html())
                }
            }
        }
    }

}

fun Element.srinkHtml(): Element {
    this.select("*").forEach { node ->
        if (node.hasAttr("class")) node.removeAttr("class")
        if (node.hasAttr("style")) node.removeAttr("style")
        if (node.tagName() == "script") node.remove()
        if (node.tagName() == "svg") node.remove()
    }

    return this
}


