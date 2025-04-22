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
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.LLMAttachment
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.MatchInfo
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.ScrapePipeline
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import java.time.Duration

internal class ScraperPipelineContainer(
    private val browserPool: PlaywrightBrowserPool
) {
    private val basketballScrapePipeline = FlashscoreBasketballScrapePipeline(browserPool)
    private val hockeyScrapePipeline = FlashscoreHockeyScrapePipeline(browserPool)
    private val baseballScrapePipeline = SpojoyBaseballScrapePipeline(browserPool)

    fun getByLeague(league: League): ScrapePipeline<out MatchInfo, out League> = when (league) {
        is League.Basketball -> this.basketballScrapePipeline
        is League.Hockey -> this.hockeyScrapePipeline
        is League.Baseball -> this.baseballScrapePipeline
    }
}

// TODO: enum에 파일 프로퍼티를 추가하든 해야할 듯. 매호출마다 파일을 다시 읽어드림...
internal fun readResourceFile(path: String): String = object {}::class.java.getResource(path)?.readText() ?: throw Exception("can't find ${path} in resource")

internal fun getPromptByLeague(league: League): String = when (league) {
    is League.Basketball -> readResourceFile("/basketball-prompt.txt")
    is League.Hockey -> readResourceFile("/hockey-prompt.txt")
    is League.Baseball -> readResourceFile("/baseball-prompt.txt")
}

internal suspend fun scrapeAndGeneratePick(
    claudeClient: AnthropicClient,
    sportsMatchRepo: SportsMatchRepository,
    pickRepo: PickRepository,
    scrapePipelineContainer: ScraperPipelineContainer,
    leagues: Set<League>,
    filteringUrls: Set<String>
) = coroutineScope {
    for (league in leagues) {
        val scrapePipeline = scrapePipelineContainer.getByLeague(league) // 1. league로 ScrapePipelineContainer가져오기
        val matchUrls = scrapePipeline.getFixtureUrl(league).subtract(filteringUrls) // 2. match page url들 가져오기.(이미 디비에 저장된 데이터의 중복을 피하기 위해 filteringUrls 를 제외하고)
        with(scrapePipeline) { scrape(matchUrls.toList()) }
            .consumeEach { (matchInfo: MatchInfo, scrapeResult: LLMAttachment) ->
                val generatedPick: ClaudeResp = claudeClient.requestAsync(2, Duration.ofSeconds(60)) { // 3. pick 글 생성
                    model(Model.CLAUDE_3_7_SONNET_20250219)
                    maxTokens(4000L)
                    systemOfTextBlockParams(
                        listOf(
                            TextBlockParam.builder()
                                .text(getPromptByLeague(league))
                                .cacheControl(CacheControlEphemeral.builder().build())
                                .build()
                        )
                    )
                    addUserMessage(scrapeResult.toLLMAttachment())
                }
                val matchId = sportsMatchRepo.insertMatchAndGetId(SportsMatchDto.from(matchInfo)) // 4. db에 경기정보와 pick 글 저장
                pickRepo.insertAndGetId(matchId, generatedPick.message, generatedPick.inputTokens, generatedPick.outputTokens)
            }
    }
}


