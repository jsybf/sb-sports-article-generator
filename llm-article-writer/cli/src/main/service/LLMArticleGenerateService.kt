package io.gitp.llmarticlewriter.cli.service

import com.anthropic.client.AnthropicClient
import com.anthropic.models.messages.Model
import io.gitp.llmarticlewriter.database.ArticleDto
import io.gitp.llmarticlewriter.database.ScrapedPageDto
import io.gitp.llmarticlewriter.database.SportsRepository
import io.gitp.llmarticlewriter.llmwriterv2.requestStreaming


private fun readResourceFile(path: String): String = object {}::class.java.getResource(path)!!.readText()
private fun ScrapedPageDto.toLLMQuery(): String {
    return """
        <matchSummary>
        ${this.summary}
        </matchSummary>
        <winOrLooseBetCurrent>
        ${this.oneXTwoBet}
        </winOrLooseBetCurrent>
        <totalScoreBetCurrent>
        ${this.overUnderBet}
        </totalScoreBetCurrent>
    """.trimIndent()
}

internal class LLMArticleGenerateService(
    private val claudeClient: AnthropicClient,
    private val repo: SportsRepository,
    private val systemPrompt: String = readResourceFile("/prompt.txt")
) {
    fun generateAndInsertArticle() {
        repo
            .findNotGeneratedMatches()
            .asSequence()
            .map { (matchInfo, scrapedPage) ->
                val resp = claudeClient.requestStreaming {
                    model(Model.CLAUDE_3_7_SONNET_LATEST)
                    maxTokens(2048L)
                    system(systemPrompt)
                    addUserMessage(scrapedPage.toLLMQuery())
                }
                repo.insertArticle(matchInfo.id!!, ArticleDto(null, resp.message, resp.inputTokens, resp.outputTokens))
            }
            .forEach { }
    }
}