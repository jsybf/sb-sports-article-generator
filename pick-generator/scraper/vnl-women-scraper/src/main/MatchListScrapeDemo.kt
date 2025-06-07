package io.gitp.sbpick.pickgenerator.scraper.vnlwommenscraper

import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.VnlWommenTeam
import java.sql.DriverManager
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*

private data class VnlWommenMatchInfo(
    val homeTeam: VnlWommenTeam,
    val awayTeam: VnlWommenTeam,
    val matchAt: LocalDateTime,
    val matchPageUrl: String
)

private suspend fun scrapeSofascoreMatchLists(browserPool: PlaywrightBrowserPool): List<VnlWommenMatchInfo> {
    val matchesPageUrl = "https://www.sofascore.com/tournament/volleyball/international/nations-league-women/11094#id:70644,tab:matches"

    val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyHH:mm")

    val doc = browserPool.doAndGetDocAsync {
        navigate(matchesPageUrl)
        waitForTimeout(2000.0)
    }.await()

    return doc
        .select(".Box.kiSsvW > a")
        .drop(2)
        .map { row ->
            val bidElements = row.select("bdi")
            val homeTeam = bidElements[2].text().let { VnlWommenTeam.fromSofascore(it) }
            val awayTeam = bidElements[3].text().let { VnlWommenTeam.fromSofascore(it) }

            val dateStr = bidElements[0].text()
            val timeStr = bidElements[1].text()

            val matchAt = runCatching { LocalDateTime.parse(dateStr + timeStr, dateTimeFormatter) }.getOrDefault(LocalDateTime.of(2020, 1, 1, 1, 1))

            val url = row.selectFirst("a")!!.attr("href").let { "https://www.sofascore.com" + it }
            VnlWommenMatchInfo(
                homeTeam,
                awayTeam,
                matchAt,
                url
            )
        }
}


private val flashscoreDateTimeForamt = DateTimeFormatterBuilder()
    .appendPattern("dd.MM. HH:mm")
    .parseDefaulting(ChronoField.YEAR, LocalDateTime.now().year.toLong())
    .toFormatter()
    .withLocale(Locale.KOREAN)

private suspend fun scrapeFlashscoreMatchLists(browserPool: PlaywrightBrowserPool): List<VnlWommenMatchInfo> {
    val matchesPageUrl = "https://www.flashscore.co.kr/volleyball/world/nations-league-women/fixtures"

    val doc = browserPool.doAndGetDocAsync {
        navigate(matchesPageUrl)
        waitForTimeout(2000.0)
    }.await()

    return doc
        .select(".event__match.event__match--withRowLink.event__match--static.event__match--scheduled.event__match--twoLine")
        .map { element ->
            val matchAt = element.selectFirst(".event__time")!!.text().let { LocalDateTime.parse(it, flashscoreDateTimeForamt) }
            val homeTeam = element.selectFirst(".event__participant--home")!!.text().let { VnlWommenTeam.fromFlashscore(it) }
            val awayTeam = element.selectFirst(".event__participant--away")!!.text().let { VnlWommenTeam.fromFlashscore(it) }
            val flashscoreDetailPage = element.selectFirst("a.eventRowLink")!!.attr("href")

            VnlWommenMatchInfo(
                awayTeam = awayTeam,
                homeTeam = homeTeam,
                matchAt = matchAt,
                matchPageUrl = flashscoreDetailPage
            )
        }
}


suspend fun main() {
    insertMatchesToMysql()
}


suspend fun insertMatchesToMysql() {
    val browserPool = PlaywrightBrowserPool(1)
    val flashscoreMatches = scrapeFlashscoreMatchLists(browserPool)
    val sofascoreMatches = scrapeSofascoreMatchLists(browserPool)

    val sofaMatches = sofascoreMatches.sortedBy { it.matchAt } // .filter { LocalDateTime.now().plusDays(1) < it.matchAt }
    val flashMatches = flashscoreMatches.sortedBy { it.matchAt }

    val conn = DriverManager.getConnection(
        "jdbc:mysql://54.180.248.188:3306/sb-pick",
        System.getenv("SB_PICK_MYSQL_USER")!!,
        System.getenv("SB_PICK_MYSQL_PW")!!
    )
    val flashInsertSql = """
        INSERT INTO  flash_vnl_wommen_match (home_team, away_team, match_at, url) 
        VALUES (?, ?, ?, ?)
    """.trimIndent()
    val sofaInsertSql = """
        INSERT INTO  sofa_vnl_wommen_match (home_team, away_team, match_at, url) 
        VALUES (?, ?, ?, ?)
    """.trimIndent()

    conn.prepareStatement(flashInsertSql).use { stat ->
        flashMatches.forEach { match ->
            stat.setString(1, match.homeTeam.toString())
            stat.setString(2, match.awayTeam.toString())
            stat.setTimestamp(3, Timestamp.valueOf(match.matchAt))
            stat.setString(4, match.matchPageUrl)
            stat.addBatch()
        }
        stat.executeBatch()
    }
    conn.prepareStatement(sofaInsertSql).use { stat ->
        sofaMatches.forEach { match ->
            stat.setString(1, match.homeTeam.toString())
            stat.setString(2, match.awayTeam.toString())
            stat.setTimestamp(3, Timestamp.valueOf(match.matchAt))
            stat.setString(4, match.matchPageUrl)
            stat.addBatch()
        }
        stat.executeBatch()
    }

    conn.close()
    browserPool.close()
}
