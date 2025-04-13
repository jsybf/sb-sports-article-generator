package scrape.hockey

import kotlinx.serialization.json.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.nio.file.Path
import kotlin.io.path.readText

fun buildOddsJson(doc: Document): JsonArray {
    val oddList = doc.select(".ui-table__body .ui-table__row").map { rowElement ->
        buildJsonObject {
            put(
                "bookmaker",
                rowElement.selectFirst("a.prematchLink")!!.attr("title")
            )
            put(
                "total",
                rowElement.selectFirst(".wcl-oddsValue_Fc9sZ")!!.text().toFloat()
            )
            put(
                "over",
                rowElement.selectFirst("a[data-analytics-element=\"ODDS_COMPARIONS_ODD_CELL_2\"] > span")?.text()?.toFloat()
            )
            put(
                "under",
                rowElement.selectFirst("a[data-analytics-element=\"ODDS_COMPARIONS_ODD_CELL_3\"] > span")?.text()?.toFloat()
            )
        }
    }

    return buildJsonArray { oddList.forEach { add(it) } }
}

fun main() {
    // 실제 사용 예시
    val sample = Path.of("test-data/odd.html").readText()

    buildOddsJson(Jsoup.parse(sample)).let { println(it) }
    // val jsonOutput = parser.parseHtml(sample)
    // println(jsonOutput)
}

