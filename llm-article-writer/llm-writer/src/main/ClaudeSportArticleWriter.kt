import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.models.messages.MessageCreateParams
import com.anthropic.models.messages.Model


class ClaudeSportArticleWriter(
    private val apiKey: String
) {

    private val client = AnthropicOkHttpClient.builder()
        .apiKey(apiKey)
        .build()

    private val prompt: String = """
            너는 스포츠 분석을 잘하는 40대 남자야.
            예정된 경기 정보가 담긴 html코드 일부들이 주어지면 너가 스포츠 경기결과를 예측/분석을 하는 글을 작성해줘.
            
            html코드조각은 "---html에 관한 간단한 소개"로 구분이 되어 주어지며
            첫번째 html 코드 조각은 경기의 요약 정보들
            그뒤로 나오는 html 조각은 결장선수들에 관한 정보야
            
            일단 html에서 의미있는 정보들을 추출한다음 글을 쓰는 식으로 작업을 진행해줘.
            
            
            예측/분석을 할때 아래와 같은 사항을 지켜줘
            - 
            - 이모지들을 너무 많진 않지만 살짝 달아줘 글 초반에 
            - 구체적인 수치를 바탕으로 근거있는 주장하기
            - 단순 수치 나열이 아닌 너만의 의견을 담기
            - 진지하지만 흡입력있는 글을 써.
            - 40대 말투로. 반말을 쓰지 않음.
            - 마크다운형식이 아닌 그냥 문장형태로.
            - 무뚜뚝한 말투로. 친근한척 하지마.
            - 가독성 좋게 문단을 나눠줘.(3~4문단 내외)
            - 글은 1500내외로 맞춰줘.
            
    """.trimIndent()

    fun generateArticle(attachment: String): String {
        val query = buildString {
            append(prompt)
            append("\n")
            append(attachment)
            append("\n")
        }
        val param: MessageCreateParams = MessageCreateParams.builder()
            .maxTokens(4096L)
            .addUserMessage(query)
            .model(Model.CLAUDE_3_5_HAIKU_LATEST)
            .build()

        println("[INFO] request claude to generate article")
        val respBuilder = StringBuilder()
        client.async().messages().createStreaming(param)
            .subscribe { chunk ->
                chunk.contentBlockDelta().stream()
                    .flatMap { deltaEvent -> deltaEvent.delta().text().stream() }
                    .forEach { textDelta ->
                        println(textDelta.text())
                        respBuilder.append(textDelta.text())
                    }
            }
            .onCompleteFuture()
            .join()
        return respBuilder.toString()
    }

    // fun generateArticle(attachment: LLMQueryAttachment): String = generateArticle(attachment.toLLMQueryAttachment())

    fun close() {
        this.client.close()
    }
}