package io.gitp.sbpick.pickgenerator.scraper.baseballscraper.extractors

import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.models.BaseballPlayerListPage
import java.net.URI

private val extractPlayerCode = Regex("""[0-9]+""")
internal fun BaseballPlayerListPage.extractPlayerPageUrl(): List<URI> {
    return this.doc
        .select("""tr[height="50"]""")
        .asIterable()
        .filter { element -> element.select("td:nth-child(4)").text() != "P" }
        .map { element -> element.select("""a[target="player"]""") }
        .map { element ->
            element.attr("onclick")
                .let { onClickAttr -> extractPlayerCode.find(onClickAttr)!!.value.toInt() }
                .let { playerCode -> "https://www.spojoy.com/sportsinfo/infomation/club/player_info.spo?player_code=${playerCode}" }
        }
        .distinct()
        .map { URI(it) }
}
