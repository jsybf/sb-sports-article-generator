package io.gitp.llmarticlewriter.service

import io.gitp.llmarticlewriter.database.*
import io.gitp.llmarticlewriter.database.repo.HockeyRepo
import io.gitp.llmarticlewriter.llmwriter.ClaudeSportArticleWriter
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

private val db = Database.connect(
    url = "jdbc:sqlite:./test-data/demo-sqlite-1.db",
    driver = "org.sqlite.JDBC",
    databaseConfig = DatabaseConfig { sqlLogger = ExposedLogger }
)

private val writer = System.getenv("CLAUDE_API_KEY")
    ?.let { apikey -> ClaudeSportArticleWriter(apiKey = apikey) }
    ?: throw IllegalStateException("env CLAUDE_API_KEY deosn't exist")

private val repo = HockeyRepo(db)

private val hockeyArticleWriteService = HockeyArticleWriteService(repo, writer)

fun scrapDemo() {
    hockeyArticleWriteService.scrape()
}

fun generateArticlesDemo() {
    hockeyArticleWriteService.generateArticle()
}

fun main() {
    transaction(db) {
        SchemaUtils.create(
            HockeyMatchTbl,
            HockeyMatchSummaryPageTbl,
            HockeyPlayerPageTbl,
            HockeyLLMArticleTbl
        )
    }
    scrapDemo()
    // generateArticlesDemo()
}