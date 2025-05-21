package io.gitp.sbpick.pickgenerator.pickgenerator

import com.anthropic.client.AnthropicClient
import com.anthropic.models.messages.CacheControlEphemeral
import com.anthropic.models.messages.Model
import com.anthropic.models.messages.TextBlockParam
import io.gitp.pickgenerator.claude.models.ClaudeResp
import io.gitp.pickgenerator.claude.requestAsync
import io.gitp.sbpick.pickgenerator.database.models.SportsMatchDto
import io.gitp.sbpick.pickgenerator.database.repositories.PickRepository
import io.gitp.sbpick.pickgenerator.database.repositories.SportsMatchRepository
import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.BaseballScrapePipeline
import io.gitp.sbpick.pickgenerator.scraper.basketballscraper.FlashscoreBasketballScrapePipeline
import io.gitp.sbpick.pickgenerator.scraper.hockeyscraper.FlashscoreHockeyScrapePipeline
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.RequiredPageNotFound
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.LLMAttachment
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.MatchInfo
import io.gitp.sbpick.pickgenerator.scraper.soccerminorscraper.FlashscoreMinorSoccerScrapePipeline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.time.LocalDate
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private fun readResourceFile(path: String): String = object {}::class.java.getResource(path)?.readText() ?: throw Exception("can't find ${path} in resource")

private object PromptContainer {
    private val hockeyPrompt = readResourceFile("/hockey-prompt.txt")
    private val basketballPrompt = readResourceFile("/basketball-prompt.txt")
    private val baseballPrompt = readResourceFile("/baseball-prompt.txt")
    private val minorSoccerPrompt = readResourceFile("/minor-soccer-prompt.txt")

    fun findByLeague(league: League): String = when (league) {
        is League.Hockey -> hockeyPrompt
        is League.Baseball -> baseballPrompt
        is League.Basketball -> basketballPrompt
        is League.MinorSoccer -> minorSoccerPrompt
    }
}

internal interface ScrapeService<L : League> {
    suspend fun scrape(browserPool: PlaywrightBrowserPool, league: L, matchAt: LocalDate): Flow<Result<Pair<MatchInfo, LLMAttachment>>>
}

internal object BaseballScrapeService : ScrapeService<League.Baseball> {
    override suspend fun scrape(browserPool: PlaywrightBrowserPool, league: League.Baseball, matchAt: LocalDate): Flow<Result<Pair<MatchInfo, LLMAttachment>>> {
        return BaseballScrapePipeline
            .scrapeFixtures(browserPool, league, matchAt)
            .asFlow()
            .map { matchInfo -> BaseballScrapePipeline.scrapeMatch(browserPool, matchInfo).mapCatching { Pair(matchInfo, it) } }
    }
}

internal object HockeyScrapeService : ScrapeService<League.Hockey> {
    override suspend fun scrape(browserPool: PlaywrightBrowserPool, league: League.Hockey, matchAt: LocalDate): Flow<Result<Pair<MatchInfo, LLMAttachment>>> {
        return FlashscoreHockeyScrapePipeline
            .scrapeFixtures(browserPool, league, matchAt)
            .asFlow()
            .map { matchInfo -> FlashscoreHockeyScrapePipeline.scrapeMatch(browserPool, matchInfo).mapCatching { Pair(matchInfo, it) } }
    }
}

internal object BasketballScrapeService : ScrapeService<League.Basketball> {
    override suspend fun scrape(browserPool: PlaywrightBrowserPool, league: League.Basketball, matchAt: LocalDate): Flow<Result<Pair<MatchInfo, LLMAttachment>>> {
        return FlashscoreBasketballScrapePipeline
            .scrapeFixtures(browserPool, league, matchAt)
            .asFlow()
            .map { matchInfo -> FlashscoreBasketballScrapePipeline.scrapeMatch(browserPool, matchInfo).mapCatching { Pair(matchInfo, it) } }
    }
}


internal object MinorSoccerScrapeService : ScrapeService<League.MinorSoccer> {
    override suspend fun scrape(browserPool: PlaywrightBrowserPool, league: League.MinorSoccer, matchAt: LocalDate): Flow<Result<Pair<MatchInfo, LLMAttachment>>> {
        return FlashscoreMinorSoccerScrapePipeline
            .scrapeFixtures(browserPool, league, matchAt)
            .asFlow()
            .map { matchInfo -> FlashscoreMinorSoccerScrapePipeline.scrapeMatch(browserPool, matchInfo).mapCatching { Pair(matchInfo, it) } }
    }
}


suspend fun PlaywrightBrowserPool.scrape(
    league: League,
    excludeMatches: Set<MatchInfo>,
    matchAt: LocalDate
): Flow<Result<Pair<MatchInfo, LLMAttachment>>> {
    return when (league) {
        is League.Basketball -> BasketballScrapeService.scrape(this, league, matchAt)
        is League.Baseball -> BaseballScrapeService.scrape(this, league, matchAt)
        is League.Hockey -> HockeyScrapeService.scrape(this, league, matchAt)
        is League.MinorSoccer -> MinorSoccerScrapeService.scrape(this, league, matchAt)
    }
        .filterNot { scrapeResult: Result<Pair<MatchInfo, LLMAttachment>> ->
            scrapeResult
                .getOrNull()
                ?.first
                ?.let { matchInfo -> excludeMatches.any { it.isEqual(matchInfo) } }
                ?: false
        }
}

internal suspend fun AnthropicClient.generatePick(
    scrapedResult: LLMAttachment,
    prompt: String,
    maxRetry: Int = 2,
    duration: Duration = 60.seconds,
): ClaudeResp {
    val generatedPick: ClaudeResp = this.requestAsync(maxRetry, duration) {
        model(Model.CLAUDE_3_7_SONNET_20250219)
        maxTokens(8000L)
        systemOfTextBlockParams(
            listOf(
                TextBlockParam.builder()
                    .text(prompt)
                    .cacheControl(CacheControlEphemeral.builder().build())
                    .build()
            )
        )
        addUserMessage(scrapedResult.toLLMAttachment())
    }
    logger.info("inputTokens:{} outputTokens:{}", generatedPick.inputTokens, generatedPick.outputTokens)
    return generatedPick
}

@OptIn(ExperimentalCoroutinesApi::class)
internal suspend fun scrapeGeneratePersistPick(
    leagues: Set<League>,
    excludeMatches: Set<MatchInfo>,
    browserPool: PlaywrightBrowserPool,
    claudeClient: AnthropicClient,
    sportsMatchRepo: SportsMatchRepository,
    pickRepo: PickRepository,
    matchAt: LocalDate
    // maxRetry: Int = 2,
    // duration: Duration = 60.seconds,
) {
    leagues
        .asFlow()
        .map { league -> browserPool.scrape(league, excludeMatches, matchAt) }
        .flattenConcat()
        .map { scrapeResult: Result<Pair<MatchInfo, LLMAttachment>> ->
            val (matchInfo, scrapedResult) = scrapeResult.getOrElse { exception ->
                when (exception) {
                    is RequiredPageNotFound -> {
                        logger.warn(exception.message)
                        return@map null
                    }
                    else -> throw exception
                }
            }
            val pickResp: ClaudeResp = withContext(Dispatchers.IO) {
                claudeClient.generatePick(scrapedResult, PromptContainer.findByLeague(matchInfo.league))
            }
            Pair(matchInfo, pickResp)
        }
        .filterNotNull()
        .collect { (matchInfo: MatchInfo, pickResp: ClaudeResp) ->
            val matchId = sportsMatchRepo.insertMatchAndGetId(SportsMatchDto.from(matchInfo))
            pickRepo.insertAndGetId(matchId, pickResp.message, pickResp.inputTokens, pickResp.outputTokens)
        }
}
