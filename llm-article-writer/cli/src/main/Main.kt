package io.gitp.llmarticlewriter.cli

import HockeyScrapeService
import PlaywrightBrowser
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.enum
import io.gitp.llmarticlewriter.database.*
import model.HockeyMatchInfo
import model.League
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun HockeyMatchInfo.toHockeyMatchDto() = HockeyMatchDto(
    id = null,
    homeTeam = homeTeam,
    awayTeam = awayTeam,
    startAt = matchAt,
    league = league,
    matchPageUrl = matchPageUrl
)

fun HockeyMatchInfo.toHockeyScrapedPageDto() = HockeyScrapedPageDto(
    id = null,
    summary = matchSummary.toString(),
    oneXTwoBet = oneXTwoBet.toString(),
    overUnderBet = overUnderBet.toString()
)

private class ScrapeCmdRoot : CliktCommand(name = "scrape") {
    override fun run() = Unit
}

private class HockeyScrapeCmd : CliktCommand(name = "hockey") {
    val league: League by option("--league", "-l").enum<League>().required()
    override fun run() {

        val repo = getDBConnection("jdbc:sqlite:./test-data/dev-sqlite.db")
            .also { db -> transaction(db) { SchemaUtils.create(HockeyScrapedTbl, HockeyArticleTbl, HockeyScrapedTbl) } }
            .let { db -> HockeyRepo(db) }

        PlaywrightBrowser().use { browser ->
            HockeyScrapeService(browser)
                .scrapeUpcommingMatch(league)
                .forEach { matchInfo ->
                    val matchId = repo.insertHockeyMatch(matchInfo.toHockeyMatchDto())
                    repo.insertHockeyScrapedPage(matchId, matchInfo.toHockeyScrapedPageDto())
                }
        }
    }
}

private class ArticleGenerateCmdRoot : CliktCommand(name = "generate") {
    override fun run() = Unit
}

private class HockeyArticleGenerateCmd : CliktCommand(name = "hockey") {
    val league: League by option("--league", "-l").enum<League>().required()
    override fun run() {
        echo("selected league is ${league}")
    }
}

private class ExportCmd : CliktCommand(name = "export") {
    override fun run() = Unit
}

private class RootCmd : CliktCommand("saw") {
    override fun run() = Unit
}

fun main(args: Array<String>) = RootCmd()
    .subcommands(
        ScrapeCmdRoot().subcommands(HockeyScrapeCmd()),
        ArticleGenerateCmdRoot().subcommands(HockeyArticleGenerateCmd()),
        ExportCmd()
    ).main(args)
