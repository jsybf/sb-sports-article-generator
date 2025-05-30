package io.gitp.sbpick.pickgenerator.pickgenerator

import ch.qos.logback.classic.Level
import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.split
import io.gitp.sbpick.pickgenerator.database.models.SportsMatchDto
import io.gitp.sbpick.pickgenerator.database.repositories.PickRepository
import io.gitp.sbpick.pickgenerator.database.repositories.SportsMatchRepository
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.MatchInfo
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.LocalDateTime

class ScrapeThenGenerateCommand : CliktCommand("scrape-gene") {
    private val mysqlHost by option("--mysql_host", envvar = "SB_PICK_MYSQL_HOST")
    private val mysqlPort by option("--mysql_port", envvar = "SB_PICK_MYSQL_PORT")
    private val mysqlUser by option("--mysql_user", envvar = "SB_PICK_MYSQL_USER")
    private val mysqlPassword by option("--mysql_pw", envvar = "SB_PICK_MYSQL_PW")
    private val mysqlDatabase by option("--mysql_db", envvar = "SB_PICK_MYSQL_DB")

    private val claudeApiKey by option("--claude_api_key", envvar = "SB_PICK_CLAUDE_API_KEY")

    private val allFlag: Boolean by option("--all").flag()
    private val excludes: List<String>? by option("--exclude").split(",")
    private val includes: List<String>? by option("--include").split(",")

    private val logLevel: Level? by option("--log-level").convert { Level.toLevel(it) }

    private data class CommonMatchInfo(
        override val awayTeam: String,
        override val homeTeam: String,
        override val matchAt: LocalDateTime,
        override val league: League,
    ) : MatchInfo {
        companion object {
            fun from(sportsMatchDto: SportsMatchDto): CommonMatchInfo = CommonMatchInfo(
                awayTeam = sportsMatchDto.awayTeam,
                homeTeam = sportsMatchDto.homeTeam,
                matchAt = sportsMatchDto.matchAt,
                league = sportsMatchDto.league
            )
        }
    }

    override fun run() {
        // validate arguments
        if (!allFlag && includes == null) {
            echo("use --all or --include to select leagues")
        }
        if (allFlag && includes != null) {
            echo("can't use both of --all and --include")
        }
        // get leagues from arguments
        val leagues = if (allFlag) {
            val excludeLeagues: Set<League>? = excludes?.flatMap { parseLeagueArgument(it) }?.toSet()
            League.allEntries.toSet().subtract(excludeLeagues ?: emptySet())
        } else {
            val includeLeagues: Set<League> = includes!!.flatMap { parseLeagueArgument(it) }.toSet()
            val excludeLeagues: Set<League>? = excludes?.flatMap { parseLeagueArgument(it) }?.toSet()
            includeLeagues.subtract(excludeLeagues ?: emptySet())
        }

        val db = Database.connect(
            url = "jdbc:mysql://${mysqlHost!!}:${mysqlPort!!}/${mysqlDatabase!!}".also { println("jdbc_url: ${it}") },
            user = mysqlUser!!,
            password = mysqlPassword!!
        )

        val sportsMatchRepo = SportsMatchRepository(db)
        val pickRepo = PickRepository(db)
        val browserPool = PlaywrightBrowserPool(4)
        val claudeClient = AnthropicOkHttpClient.builder().apiKey(claudeApiKey!!).build()

        val existingMatches: Set<MatchInfo> = sportsMatchRepo.findFixtures().map { CommonMatchInfo.from(it) as MatchInfo }.toSet()

        if (logLevel != null) {
            val rootLogger = LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME) as ch.qos.logback.classic.Logger
            rootLogger.level = logLevel
        }
        runBlocking {
            scrapeGeneratePersistPick(
                leagues = leagues,
                excludeMatches = existingMatches,
                browserPool = browserPool,
                claudeClient = claudeClient,
                sportsMatchRepo = sportsMatchRepo,
                matchAt = LocalDate.now(),
                pickRepo = pickRepo,
            )
        }
        runBlocking {
            scrapeGeneratePersistPick(
                leagues = leagues,
                excludeMatches = existingMatches,
                browserPool = browserPool,
                claudeClient = claudeClient,
                sportsMatchRepo = sportsMatchRepo,
                matchAt = LocalDate.now().plusDays(1),
                pickRepo = pickRepo,
            )
        }
        // runBlocking {
        //     scrapeGeneratePersistPick(
        //         leagues = leagues,
        //         excludeMatches = existingMatches,
        //         browserPool = browserPool,
        //         claudeClient = claudeClient,
        //         sportsMatchRepo = sportsMatchRepo,
        //         matchAt = LocalDate.now().plusDays(2),
        //         pickRepo = pickRepo,
        //     )
        // }
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