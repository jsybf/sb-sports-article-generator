package io.gitp.llmarticlewriter.database

import model.League
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object HockeyMatchTbl : IntIdTable("hockey_match") {
    val startAt = datetime("start_at")
    val homeTeam = text("home_team")
    val awayTeam = text("away_team")
    val updatedAt = datetime("updated_at")
    val league = enumerationByName<League>("league", 10)
    val matchPageUrl = text("match_page_url")
}

object HockeyScrapedTbl : IntIdTable("hockey_scraped") {
    val hockeyMatchId = reference("hockey_match_id", HockeyMatchTbl)
    val updatedAt = datetime("updated_at")
    val summary = text("summary")
    val oneXTwoBet = text("one_x_two_bet")
    val overUnderBet = text("over_under_bet")
}

object HockeyArticleTbl : IntIdTable("hockey_article") {
    val hockeyMatchId = reference("hockey_match_id", HockeyMatchTbl)
    val updatedAt = datetime("updated_at")
    val article = text("article")
    val inputTokens = integer("inputTokens")
    val outputTokens = integer("outputTokens")
}

class HockeyMatchEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<HockeyMatchEntity>(HockeyMatchTbl)

    var startAt by HockeyMatchTbl.startAt
    var homeTeam by HockeyMatchTbl.homeTeam
    var awayTeam by HockeyMatchTbl.awayTeam
    var updatedAt by HockeyMatchTbl.updatedAt
    var matchPageUrl by HockeyMatchTbl.matchPageUrl
    var league by HockeyMatchTbl.league

    val scraped by HockeyScrapedEntity backReferencedOn HockeyScrapedTbl.hockeyMatchId
    val article by HockeyArticleEntity optionalBackReferencedOn HockeyArticleTbl.hockeyMatchId
}

class HockeyScrapedEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<HockeyScrapedEntity>(HockeyScrapedTbl)

    var hockeyMatchEntity by HockeyMatchEntity referencedOn HockeyScrapedTbl.hockeyMatchId
    var summary by HockeyScrapedTbl.summary
    var oneXTwoBet by HockeyScrapedTbl.oneXTwoBet
    var overUnderBet by HockeyScrapedTbl.overUnderBet
}

class HockeyArticleEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<HockeyArticleEntity>(HockeyArticleTbl)

    var hockeyMatchEntity by HockeyMatchEntity referencedOn HockeyArticleTbl.hockeyMatchId
    var article by HockeyArticleTbl.article
    var inputTokens by HockeyArticleTbl.inputTokens
    var outputTokens by HockeyArticleTbl.outputTokens
}
