package io.gitp.llmarticlewriter.cli

import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.path
import com.microsoft.playwright.CLI
import io.gitp.llmarticlewriter.cli.service.LLMArticleGenerateService
import io.gitp.llmarticlewriter.cli.service.ScrapeService
import io.gitp.llmarticlewriter.database.MatchInfoDto
import io.gitp.llmarticlewriter.database.ScrapedPageDto
import io.gitp.llmarticlewriter.database.SportsRepository
import io.gitp.llmarticlewriter.database.getSqliteConn
import io.gitp.llmarticlewriter.scraper.model.League
import io.gitp.llmarticlewriter.scraper.model.MatchInfo
import java.nio.file.Path

fun MatchInfo.toMatchInfoDto() = MatchInfoDto(
    id = null,
    homeTeam = homeTeam,
    awayTeam = awayTeam,
    startAt = matchAt,
    league = league,
    matchPageUrl = matchPageUrl
)

fun MatchInfo.toScrapedPageDto() = ScrapedPageDto(
    id = null,
    summary = matchSummary.toString(),
    oneXTwoBet = oneXTwoBet.toString(),
    overUnderBet = overUnderBet.toString()
)

private class ScrapeCmd : CliktCommand(name = "scrape") {
    val sqlitePath: Path by option("--db").path().required()
    val allFlag: Boolean by option("--all").flag(default = false)
    val sports: String? by option("--sports", "-s")
    val league: String? by option("--league", "-l")

    override fun run() {
        val scrapeService = ScrapeService(SportsRepository(getSqliteConn(sqlitePath)))

        if (allFlag) {
            League.allLeagues.forEach { league -> scrapeService.scrapeLeague(league) }
            return
        }

        require(sports != null && league != null) { "if not --all option is set, bro you should provide --sports and --league " }
        val league = runCatching { League.ofName(sports!!, league!!) }
            .onFailure { throw Exception("can't invalide sports=${sports!!} league=${league!!}. use show command to see supported sports, league pair") }
            .getOrNull()!!
        scrapeService.scrapeLeague(league)
    }
}

private class ShowSupportedSportsLeagueCmd : CliktCommand(name = "show") {
    override fun run() {
        println("""${"sports".padEnd(15)} ${"league".padEnd(10)}""")
        println("""${"-".repeat(15)} ${"-".repeat(10)}""")
        League.allLeagues.forEach { league ->
            println("""${league.sportsName.padEnd(15)} ${league.leagueName.padEnd(10)}""")
        }
    }
}

private class ArticleGenerateCmdRoot : CliktCommand(name = "generate") {
    val sqlitePath: Path by option("--db").path().required()

    override fun run() {
        val claudeApiKey = System.getenv("CLAUDE_API_KEY") ?: throw Exception("env CLAUDE_API_KEY is missing")
        val articleGenerateService = LLMArticleGenerateService(
            claudeClient = AnthropicOkHttpClient.builder().apiKey(claudeApiKey).build(),
            repo = SportsRepository(getSqliteConn(sqlitePath))
        )
        articleGenerateService.generateAndInsertArticle()
    }
}

private class InstallBrowserCmd : CliktCommand(name = "install-browser") {
    override fun run() {
        CLI.main(arrayOf("install", "--with-deps", "chromium"))
    }
}

private class RootCmd : CliktCommand("root") {
    override fun run() = Unit
}

fun main(args: Array<String>) = RootCmd()
    .subcommands(
        ScrapeCmd(),
        ArticleGenerateCmdRoot(),
        InstallBrowserCmd(),
        ShowSupportedSportsLeagueCmd()
    ).main(args)
