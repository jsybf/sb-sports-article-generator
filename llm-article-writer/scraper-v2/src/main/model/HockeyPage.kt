package model

import org.jsoup.nodes.Document


object HockeyPage {
    data class UpcommingMatcListhPage(
        val doc: Document
    )

    data class MatchPage(
        val doc: Document
    )

    data class OneXTwoBetPage(
        val doc: Document
    )

    data class OverUnderBetPage(
        val doc: Document
    )
}