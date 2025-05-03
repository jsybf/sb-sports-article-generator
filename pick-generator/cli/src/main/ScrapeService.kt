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
import io.gitp.sbpick.pickgenerator.scraper.baseballscraper.SpojoyBaseballScrapePipeline
import io.gitp.sbpick.pickgenerator.scraper.basketballscraper.FlashscoreBasketballScrapePipeline
import io.gitp.sbpick.pickgenerator.scraper.hockeyscraper.FlashscoreHockeyScrapePipeline
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.RequiredPageNotFound
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.LLMAttachment
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.MatchInfo
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.ScrapePipeline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

// TODO: enum에 파일 프로퍼티를 추가하든 해야할 듯. 매호출마다 파일을 다시 읽어드림...
private fun readResourceFile(path: String): String = object {}::class.java.getResource(path)?.readText() ?: throw Exception("can't find ${path} in resource")

private object ScraperModuleContainer {
    data class ScraperModule(
        val prompt: String,
        val scrapePipline: ScrapePipeline<League>
    )

    private val hockeyModule = ScraperModule(readResourceFile("/hockey-prompt.txt"), FlashscoreHockeyScrapePipeline)
    private val baseketballModule = ScraperModule(readResourceFile("/basketball-prompt.txt"), FlashscoreBasketballScrapePipeline)
    private val baseballModule = ScraperModule(readResourceFile("/baseball-prompt.txt"), SpojoyBaseballScrapePipeline)

    fun findByLeague(league: League): ScraperModule = when (league) {
        is League.Hockey -> hockeyModule
        is League.Baseball -> baseballModule
        is League.Basketball -> baseketballModule
    }
}

internal fun PlaywrightBrowserPool.scrape(
    league: League,
    filteringUrls: Set<String>
): Flow<Result<Pair<MatchInfo, LLMAttachment>>> = flow {
    val (_, scraperPipeline) = ScraperModuleContainer.findByLeague(league)

    val matchUrls = scraperPipeline.scrapeFixtureUrls(this@scrape, league).subtract(filteringUrls)

    for (matchUrl in matchUrls) {
        emit(scraperPipeline.scrapeMatch(this@scrape, league, matchUrl))
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
    filteringUrls: Set<String>,
    browserPool: PlaywrightBrowserPool,
    claudeClient: AnthropicClient,
    sportsMatchRepo: SportsMatchRepository,
    pickRepo: PickRepository,
    maxRetry: Int = 2,
    duration: Duration = 60.seconds,
) {
    leagues
        .asFlow()
        .map { league -> browserPool.scrape(league, filteringUrls) }
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
                claudeClient.generatePick(scrapedResult, ScraperModuleContainer.findByLeague(matchInfo.league).prompt)
            }
            Pair(matchInfo, pickResp)
        }
        .filterNotNull()
        .collect { (matchInfo: MatchInfo, pickResp: ClaudeResp) ->
            val matchId = sportsMatchRepo.insertMatchAndGetId(SportsMatchDto.from(matchInfo))
            pickRepo.insertAndGetId(matchId, pickResp.message, pickResp.inputTokens, pickResp.outputTokens)
        }
}