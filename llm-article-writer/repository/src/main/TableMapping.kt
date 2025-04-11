package db

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.datetime
import org.jsoup.nodes.Document
import java.time.LocalDateTime

// Table definitions
object HockeyMatchTbl : IntIdTable("hockey_match") {
    val startAt: Column<LocalDateTime> = datetime("start_at")
    val homeTeam: Column<String> = text("home_team")
    val awayTeam: Column<String> = text("away_team")
    val updatedAt: Column<LocalDateTime> = datetime("updated_at")
}

object HockeyMatchSummaryPageTbl : IntIdTable("hockey_match_summary_page") {
    val hockeyMatchId: Column<EntityID<Int>> = reference("hockey_match_id", HockeyMatchTbl)
    val page: Column<Document> = text("page").transform(HtmlColumnTransformer())
}

object HockeyPlayerPageTbl : IntIdTable("hockey_player_page") {
    val hockeyMatchId: Column<EntityID<Int>> = reference("hockey_match_id", HockeyMatchTbl)
    val page: Column<Document> = text("page").transform(HtmlColumnTransformer())
}

object HockeyLLMArticleTbl : IntIdTable("hockey_llm_article") {
    val hockeyMatchId: Column<EntityID<Int>> = reference("hockey_match_id", HockeyMatchTbl)
    val article: Column<String> = text("page")
    val updatedAt: Column<LocalDateTime> = datetime("updated_at")
}

// DAO classes
class HockeyMatch(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<HockeyMatch>(HockeyMatchTbl)

    var startAt by HockeyMatchTbl.startAt
    var homeTeam by HockeyMatchTbl.homeTeam
    var awayTeam by HockeyMatchTbl.awayTeam
    var updatedAt by HockeyMatchTbl.updatedAt

    val summaryPages by HockeyMatchSummaryPage referrersOn HockeyMatchSummaryPageTbl.hockeyMatchId
    val playerPages by HockeyPlayerPage referrersOn HockeyPlayerPageTbl.hockeyMatchId
    val llmArticles by HockeyLLMArticle referrersOn HockeyLLMArticleTbl.hockeyMatchId
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

class HockeyLLMArticle(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<HockeyLLMArticle>(HockeyLLMArticleTbl)

    var hockeyMatch by HockeyMatch referencedOn HockeyLLMArticleTbl.hockeyMatchId
    var article by HockeyLLMArticleTbl.article
    var updatedAt by HockeyLLMArticleTbl.updatedAt
}