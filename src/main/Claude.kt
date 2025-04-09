
import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.models.messages.Message
import com.anthropic.models.messages.MessageCreateParams
import com.anthropic.models.messages.Model
import java.util.concurrent.CompletableFuture

fun main() {
    val browser = PlaywrightBrowser()
    val attachData =  HockeyScraper
        .upcommingMatchSummaryUrlList(browser)
        .first()
        .let { sampleUrl ->
            scrapeHockeyArticleAttachData(
                browser,
                sampleUrl
            )
        }
    browser.close()

    val queryAttachData =  attachData.toLLMQueryString()

    val client = AnthropicOkHttpClient.builder()
        .apiKey(System.getenv("CLAUDE_API_KEY")!!)
        .build()

    val query =  buildString {
        append("""
            너는 스포츠 분석가야
            예정된 경기 정보가 담긴 html이 주어지면 너가 스포츠 경기결과를 예측/분석을 해줘

            예측/분석을 할때 아래와 같은 사항을 지켜줘
            - 구체적인 수치를 바탕으로 근거있는 주장하기
            - 단순 수치 나열이 아닌 너만의 의견을 담기
            - 독자 입장에서 글읽는 맛이 나게 글이 착착감기게 작성하기
            - 독자 입장에서 재미있는 말투로 작성하기
            - 글은 1500내외로 맞춰줘
        """.trimIndent())
        append(queryAttachData)
    }

    val param: MessageCreateParams = MessageCreateParams.builder()
        .maxTokens(2048L)
        .addUserMessage(query)
        .model(Model.CLAUDE_3_7_SONNET_20250219)
        .build()

    val message: CompletableFuture<Message> = client.async().messages().create(param)

    message.get().content().forEach { println(it.text().get().text()) }

    client.close()
}