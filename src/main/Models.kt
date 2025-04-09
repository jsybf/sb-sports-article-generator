import org.jsoup.nodes.Document

object HockeyPage {
    data class SummaryPage(
        val doc: Document
    ) {
        fun parsePlayerUrlList(): List<String> = this.doc
            .select(".lf__isReversed a")
            .map {  "https://www.flashscore.co.kr" + it.attribute("href")!!.value }
    }

    data class PlayerPage(
        val doc: Document
    )

}

data class HockeyArticleAttachData(
    val matchSummaryHtml: HockeyPage.SummaryPage,
    val absencePlayerList: List<HockeyPage.PlayerPage>
)
