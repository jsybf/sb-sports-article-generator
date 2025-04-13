package io.gitp.llmarticlewriter.service

import io.gitp.llmarticlewriter.database.ExposedLogger
import io.gitp.llmarticlewriter.database.repo.HockeyRepo
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import scrape.hockey.srinkHtml
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.writeText

private val db = Database.connect(
    url = "jdbc:sqlite:./test-data/demo-sqlite-1.db",
    driver = "org.sqlite.JDBC",
    databaseConfig = DatabaseConfig { sqlLogger = ExposedLogger }
)

/**
 * sqlite의 html 를 파일형식으로 저장
 */
fun main() {
    val repo = HockeyRepo(db)
    val basePath = Paths.get("test-data/files")
    repo.findAllMatchPageSet().forEach { pageSet ->
        val (homeTeam, awayTeam) = pageSet.matchPageSet.matchSummaryPage.parseTeam()
        val saveDir = (basePath / "$homeTeam-$awayTeam").also { it.createDirectories() }
        (saveDir / "match.html").writeText(pageSet.matchPageSet.matchSummaryPage.extractMeaningful().srinkHtml().html())
        pageSet.matchPageSet.absencePlayerPageList.forEachIndexed { idx, page ->
            (saveDir / "player-$idx.html").writeText(page.extractMeaningful().srinkHtml().html())
        }
    }
}