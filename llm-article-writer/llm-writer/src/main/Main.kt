package io.gitp.llmarticlewriter.llmwriterv2

import HockeyScrapeService
import PlaywrightBrowser
import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.models.messages.Model
import model.HockeyMatchInfo
import model.League
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

private fun readResourceFile(path: String): String = object {}::class.java.getResource(path)!!.readText()

private val systemPrompt = readResourceFile("/prompt.txt")

private fun HockeyMatchInfo.toLLMQuery(): String {
    return """
        <matchSummary> 
        ${this.matchSummary}
        </matchSummary>
        <winOrLooseBetCurrent>
        ${this.oneXTwoBet}
        </winOrLooseBetCurrent>
        <totalScoreBetCurrent> 
        ${this.overUnderBet}
        </totalScoreBetCurrent>
    """.trimIndent()
}

// private fun main() {
//     val baseDir = Path.of("./test-data/output-examples").toAbsolutePath().normalize().also { it.createDirectories() }
//     val matchList: List<HockeyMatchInfo> = PlaywrightBrowser().use { browser -> HockeyScrapeService(browser).scrapeUpcommingMatch(League.NHL) }
//     val claudeClient = AnthropicOkHttpClient.builder().apiKey(System.getenv("CLAUDE_API_KEY")!!).build()
//
//     matchList.forEach { matchInfo ->
//         val resp = claudeClient.requestStreaming {
//             model(Model.CLAUDE_3_7_SONNET_LATEST)
//             maxTokens(2048L)
//             system(systemPrompt)
//             addUserMessage(matchInfo.toLLMQuery())
//         }
//         baseDir.resolve("${matchInfo.homeTeam}_${matchInfo.awayTeam}_${matchInfo.matchAt}.txt").writeText(resp.message)
//     }
// }
