package io.gitp.llmarticlewrtier.spojoyscraper.extractor

import io.gitp.llmarticlewrtier.spojoyscraper.model.BaseballPlayerListPage

private val extractPlayerCode = Regex("""[0-9]+""")
fun BaseballPlayerListPage.extractPlayerPageUrl(): List<String> {
    return this.doc
        .select("""tr[height="50"]""")
        .asIterable()
        .filter { element -> element.select("td:nth-child(4)").text() != "P" }
        .map { element -> element.select("""a[target="player"]""") }
        .map { element ->
            val onClickAttr = element.attr("onclick")
            val playerCode = extractPlayerCode.find(onClickAttr)!!.value.toInt()
            val playerPageUrl = "https://www.spojoy.com/sportsinfo/infomation/club/player_info.spo?player_code=${playerCode}"
            playerPageUrl
        }
        .distinct()
}