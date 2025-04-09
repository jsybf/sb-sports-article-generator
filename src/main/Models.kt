import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

object HockeyPage {
    data class SummaryPage(
        val doc: Document
    ) {
        fun parsePlayerUrlList(): List<String> = this.doc
            .select(".lf__isReversed a")
            .map { "https://www.flashscore.co.kr" + it.attribute("href")!!.value }

        fun extractMeaningFul(): Element {
            return listOf(
                doc.select("div.container__livetable div.detail__breadcrumbs"),
                doc.select("div.container__livetable div.loadable.complete")
            )
                .flatten()
                .fold(Element("div"), { acc, e -> acc.appendChild(e) })
        }
    }

    data class PlayerPage(
        val doc: Document
    ) {

        fun extractMeaningFul(): Element {
            return listOf(
                doc.select("div.container__livetable.singlePageApp > #player-profile"),
                doc.select("div.container__livetable.singlePageApp > div.container__heading")
            )
                .flatten()
                .fold(Element("div"), { acc, e -> acc.appendChild(e) })
        }

    }
}

data class HockeyArticleAttachData(
    val matchSummaryHtml: HockeyPage.SummaryPage,
    val absencePlayerList: List<HockeyPage.PlayerPage>
) {
    fun toLLMQueryString(): String = buildString {
            append("---이 부분아래는 예정된 하키경기의 html 일부분이야\n")
            append(matchSummaryHtml.extractMeaningFul().html())

            absencePlayerList.forEach { absencePlayerDoc ->
                append("---이 부분아래는 결장하는 선수의 html 일부분이야\n")
                append(absencePlayerDoc.extractMeaningFul().html())
            }
        }
}

