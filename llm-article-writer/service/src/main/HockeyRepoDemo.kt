package io.gitp.llmarticlewriter.service

import io.gitp.llmarticlewriter.database.HockeyLLMArticleTbl
import io.gitp.llmarticlewriter.database.HockeyMatchSummaryPageTbl
import io.gitp.llmarticlewriter.database.HockeyMatchTbl
import io.gitp.llmarticlewriter.database.HockeyPlayerPageTbl
import io.gitp.llmarticlewriter.database.dto.HockeyDto
import io.gitp.llmarticlewriter.database.repo.HockeyRepo
import io.gitp.llmarticlewriter.llmwriter.ClaudeSportArticleWriter
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.transactions.transaction
import scrape.PlaywrightBrowser
import scrape.hockey.HockeyPage
import scrape.hockey.HockeyScraper

fun main() {
    val db = Database.connect(
        url = "jdbc:sqlite:./test-data/demo-sqlite-1.db",
        driver = "org.sqlite.JDBC",
        databaseConfig = DatabaseConfig { sqlLogger = StdOutSqlLogger }
    )
    transaction(db) {
        SchemaUtils.create(
            HockeyMatchTbl,
            HockeyMatchSummaryPageTbl,
            HockeyPlayerPageTbl,
            HockeyLLMArticleTbl
        )
    }

    val upcommingMatchPageSetList: List<HockeyPage.MatchPageSet> = PlaywrightBrowser().use { browser ->
        val brower = HockeyScraper(browser)
        listOf(
            brower.scrapeUpcommingMatch("https://www.flashscore.co.kr/match/hockey/Ghb6iXth/#/match-summary"),
        )
    }

    val repo = HockeyRepo(db)
    upcommingMatchPageSetList.forEach { repo.insertMatchPageSetAndGetId(it) }
    val hockeyMatchPageSetList: List<HockeyDto.MatchPageSetDto> = repo.findAllMatchPageSet()

    val writer = System.getenv("CLAUDE_API_KEY")
        ?.let { apikey -> ClaudeSportArticleWriter(apiKey = apikey) }
        ?: throw IllegalStateException("env CLAUDE_API_KEY deosn't exist")

    hockeyMatchPageSetList
        .forEach { dto ->
            val article = writer.generateArticle(dto.matchPageSet.toLLMQueryAttachment())
            repo.insertLLMArticleAndGetId(dto.id, article)
        }
}