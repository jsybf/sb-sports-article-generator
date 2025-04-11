package repo

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.transactions.transaction

// Table definitions
object HockeyMatchTbl : IntIdTable("hockey_match") {
    val startAt = date("start_at")
    val team1 = text("team_1")
    val team2 = text("team_2")
    val updatedAt = date("updated_at")
}

object HockeyMatchSummaryPageTbl : IntIdTable("hockey_match_summary_page") {
    val hockeyMatchId = reference("hockey_match_id", HockeyMatchTbl)
    val page = text("page")
}

object HockeyPlayerPageTbl : IntIdTable("hockey_player_page") {
    val hockeyMatchId = reference("hockey_match_id", HockeyMatchTbl)
    val page = text("page")
}

object HockeyLLMArticleTbl : IntIdTable("hockey_llm_article") {
    val hockeyMatchId = reference("hockey_match_id", HockeyMatchTbl)
    val page = text("page")
    val updatedAt = date("updated_at")
}

// DAO classes
class HockeyMatch(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<HockeyMatch>(HockeyMatchTbl)

    var startAt by HockeyMatchTbl.startAt
    var team1 by HockeyMatchTbl.team1
    var team2 by HockeyMatchTbl.team2
    var updatedAt by HockeyMatchTbl.updatedAt

    val summaryPages by HockeyMatchSummaryPage referrersOn HockeyMatchSummaryPageTbl.hockeyMatchId
    val playerPages by HockeyPlayerPage referrersOn HockeyPlayerPageTbl.hockeyMatchId
    val llmArticles by HockeyLlmArticle referrersOn HockeyLLMArticleTbl.hockeyMatchId
}

class HockeyMatchSummaryPage(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<HockeyMatchSummaryPage>(HockeyMatchSummaryPageTbl)

    var hockeyMatch by HockeyMatch referencedOn HockeyMatchSummaryPageTbl.hockeyMatchId
    var page by HockeyMatchSummaryPageTbl.page
}

class HockeyPlayerPage(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<HockeyPlayerPage>(HockeyPlayerPageTbl)

    var hockeyMatch by HockeyMatch referencedOn HockeyPlayerPageTbl.hockeyMatchId
    var page by HockeyPlayerPageTbl.page
}

class HockeyLlmArticle(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<HockeyLlmArticle>(HockeyLLMArticleTbl)

    var hockeyMatch by HockeyMatch referencedOn HockeyLLMArticleTbl.hockeyMatchId
    var page by HockeyLLMArticleTbl.page
    var updatedAt by HockeyLLMArticleTbl.updatedAt
}

fun main() {
    // Connect to database
    Database.connect("jdbc:sqlite:./test-data/sqlite.db", "org.sqlite.JDBC")

    // Create tables
    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(HockeyMatchTbl, HockeyMatchSummaryPageTbl, HockeyPlayerPageTbl, HockeyLLMArticleTbl)
    }

    // Example usage
    transaction {
        val match = HockeyMatch.new {
            startAt = java.time.LocalDate.now()
            team1 = "Team A"
            team2 = "Team B"
            updatedAt = java.time.LocalDate.now()
        }

        HockeyMatchSummaryPage.new {
            hockeyMatch = match
            page = "Summary content here"
        }
    }
}