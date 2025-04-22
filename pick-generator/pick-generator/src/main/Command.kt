package io.gitp.sbpick.pickgenerator.pickgenerator

import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.models.messages.Model
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.split
import io.gitp.pickgenerator.claude.requestStreaming
import io.gitp.sbpick.pickgenerator.database.models.PickDto
import io.gitp.sbpick.pickgenerator.database.models.SportsMatchDto
import io.gitp.sbpick.pickgenerator.database.repositories.PickRepository
import io.gitp.sbpick.pickgenerator.database.repositories.SportsMatchRepository
import io.gitp.sbpick.pickgenerator.scraper.hockeyscraper.FlashscoreHockeyScrapePipeline
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database

private fun readResourceFile(path: String): String = object {}::class.java.getResource(path)!!.readText()
private val systemPrompt: String = readResourceFile("/prompt.txt")

class ScrapeThenGenerateCommand : CliktCommand("scrape-gene") {
    val mysqlHost by option("--mysql_host", envvar = "SB_PICK_MYSQL_HOST")
    val mysqlPort by option("--mysql_port", envvar = "SB_PICK_MYSQL_PORT")
    val mysqlUser by option("--mysql_user", envvar = "SB_PICK_MYSQL_USER")
    val mysqlPassword by option("--mysql_pw", envvar = "SB_PICK_MYSQL_PW")
    val mysqlDatabase by option("--mysql_db", envvar = "SB_PICK_MYSQL_DB")

    val claudeApiKey by option("--claude_api_key", envvar = "SB_PICK_CLAUDE_API_KEY")

    val allFlag: Boolean by option("--all").flag()
    val excludes: List<String>? by option("--exclude").split(",")
    val includes: List<String>? by option("--include").split(",")

    override fun run() {
        // // validate arguments
        // if (!allFlag && includes == null) {
        //     echo("use --all or --include to select leagues")
        // }
        // if (allFlag && includes != null) {
        //     echo("can't use both of --all and --include")
        // }
        // // get leagues from arguments
        // val leagues = if (allFlag) {
        //     val excludeLeagues: Set<League>? = excludes?.flatMap { parseLeagueArgument(it) }?.toSet()
        //     League.allEntries.toSet().subtract(excludeLeagues ?: emptySet())
        // } else {
        //     val includeLeagues: Set<League> = includes!!.flatMap { parseLeagueArgument(it) }.toSet()
        //     val excludeLeagues: Set<League>? = excludes?.flatMap { parseLeagueArgument(it) }?.toSet()
        //     includeLeagues.subtract(excludeLeagues ?: emptySet())
        // }

        val db = Database.connect(
            url = "jdbc:mysql://${mysqlHost!!}:${mysqlPort!!}/${mysqlDatabase!!}",
            user = mysqlUser!!,
            password = mysqlPassword!!
        )

        // transaction(db) {
        //     SchemaUtils.create(SportsMatchTbl, PickTbl)
        // }

        val sportsMatchRepo = SportsMatchRepository(db)
        val pickRepo = PickRepository(db)
        val browserPool = PlaywrightBrowserPool(4)
        val scrapeService = ScrapeService(browserPool)
        val claude = AnthropicOkHttpClient.builder().apiKey(claudeApiKey!!).build()

        runBlocking {
            val hockyScrapePipeline = FlashscoreHockeyScrapePipeline(browserPool)
            val urls = hockyScrapePipeline.getAllFixtureUrls()
            with(hockyScrapePipeline) { scrape(urls) }
                .receiveAsFlow()
                .onEach { matchInfo ->
                    val matchId = sportsMatchRepo.insertMatchAndGetId(SportsMatchDto.fromMatchInfo(matchInfo))
                    val resp = claude.requestStreaming {
                        model(Model.CLAUDE_3_7_SONNET_LATEST)
                        maxTokens(4000L)
                        system(systemPrompt)
                        addUserMessage(matchInfo.toLLMAttachment())
                    }
                    pickRepo.insertAndGetId(
                        matchId, PickDto(
                            id = null, sportsMatchId = null, content = resp.message, inputTokens = resp.inputTokens, outputTokens = resp.outputTokens
                        )
                    )
                }
                .collect {}

        }

    }

    private fun parseLeagueArgument(leagueArg: String): Set<League> {
        val (sportsName, leagueName) = leagueArg
            .split(".")
            .also { require(it.size == 2) { "can't parse argument ${leagueArg}" } }
            .let { Pair(it[0], it[1]) }

        if (leagueName == "*") {
            return League.allEntries.filter { it.sportsName == sportsName }.toSet()
        } else {
            return League.findByName(sportsName, leagueName)?.let { setOf(it) } ?: throw Exception("can't parse argument ${leagueArg}")
        }
    }
}

fun main(args: Array<String>) = ScrapeThenGenerateCommand().main(args)

