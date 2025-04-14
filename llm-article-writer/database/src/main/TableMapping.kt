package io.gitp.llmarticlewriter.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object SportsMatchTable : IntIdTable("sports_match") {
    val startAt = datetime("start_at")
    val homeTeam = text("home_team")
    val awayTeam = text("away_team")
    val updatedAt = datetime("updated_at")
    val sport = text("sport")
    val league = text("league")
    val matchPageUrl = text("match_page_url")
}

object FlashScoreScrapedTbl : IntIdTable("flashscore_scraped") {
    val hockeyMatchId = reference("sports_match_id", SportsMatchTable)
    val updatedAt = datetime("updated_at")
    val summary = text("summary")
    val oneXTwoBet = text("one_x_two_bet")
    val overUnderBet = text("over_under_bet")
}

object ArticleTbl : IntIdTable("article") {
    val hockeyMatchId = reference("sports_match_id", SportsMatchTable)
    val updatedAt = datetime("updated_at")
    val article = text("article")
    val inputTokens = long("inputTokens")
    val outputTokens = long("outputTokens")
}

class SportsMatchEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<SportsMatchEntity>(SportsMatchTable)

    var startAt by SportsMatchTable.startAt
    var homeTeam by SportsMatchTable.homeTeam
    var awayTeam by SportsMatchTable.awayTeam
    var updatedAt by SportsMatchTable.updatedAt
    var matchPageUrl by SportsMatchTable.matchPageUrl
    var league by SportsMatchTable.league
    var sport by SportsMatchTable.sport

    val scraped by FlashScoreScrapedEntity backReferencedOn FlashScoreScrapedTbl.hockeyMatchId
    val article by ArticleEntity optionalBackReferencedOn ArticleTbl.hockeyMatchId
}

class FlashScoreScrapedEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<FlashScoreScrapedEntity>(FlashScoreScrapedTbl)

    var sportsMatchEntity by SportsMatchEntity referencedOn FlashScoreScrapedTbl.hockeyMatchId
    var summary by FlashScoreScrapedTbl.summary
    var oneXTwoBet by FlashScoreScrapedTbl.oneXTwoBet
    var overUnderBet by FlashScoreScrapedTbl.overUnderBet
}

class ArticleEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ArticleEntity>(ArticleTbl)

    var sportsMatchEntity by SportsMatchEntity referencedOn ArticleTbl.hockeyMatchId
    var article by ArticleTbl.article
    var inputTokens by ArticleTbl.inputTokens
    var outputTokens by ArticleTbl.outputTokens
}