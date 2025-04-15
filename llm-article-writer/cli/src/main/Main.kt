package io.gitp.llmarticlewriter.cli

import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.enum
import io.gitp.llmarticlewriter.cli.service.LLMArticleGenerateService
import io.gitp.llmarticlewriter.cli.service.ScrapeService
import io.gitp.llmarticlewriter.database.MatchInfoDto
import io.gitp.llmarticlewriter.database.ScrapedPageDto
import io.gitp.llmarticlewriter.database.SportsRepository
import io.gitp.llmarticlewriter.database.getSqliteConn
import io.gitp.llmarticlewriter.scraper.model.League
import io.gitp.llmarticlewriter.scraper.model.MatchInfo

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

private class ScrapeCmdRoot : CliktCommand(name = "scrape") {
    override fun run() = Unit
}

private class HockeyScrapeCmd : CliktCommand(name = "hockey") {
    val league: League.Hockey by option("--league", "-l").enum<League.Hockey>().required()
    override fun run() {
        val scrapeService = ScrapeService(SportsRepository(getSqliteConn("jdbc:sqlite:./test-data/dev-sqlite.db")))
        scrapeService.scrapeLeague(league)
    }
}

private class BasketBallScrapeCmd : CliktCommand(name = "basketball") {
    val league: League.BasketBall by option("--league", "-l").enum<League.BasketBall>().required()
    override fun run() {
        val scrapeService = ScrapeService(SportsRepository(getSqliteConn("jdbc:sqlite:./test-data/dev-sqlite.db")))
        scrapeService.scrapeLeague(league)
    }
}

private class ArticleGenerateCmdRoot : CliktCommand(name = "generate") {
    override fun run() {
        val claudeApiKey = System.getenv("CLAUDE_API_KEY") ?: throw Exception("env CLAUDE_API_KEY is missing")

        val articleGenerateService = LLMArticleGenerateService(
            AnthropicOkHttpClient.builder().apiKey(claudeApiKey).build(),
            SportsRepository(getSqliteConn("jdbc:sqlite:./test-data/dev-sqlite.db"))
        )

        articleGenerateService.generateAndInsertArticle()
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
        ScrapeCmdRoot().subcommands(HockeyScrapeCmd(), BasketBallScrapeCmd()),
        ArticleGenerateCmdRoot(),
        ExportCmd()
    ).main(args)
