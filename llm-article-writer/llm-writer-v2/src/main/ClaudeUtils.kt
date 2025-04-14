package io.gitp.llmarticlewriter.llmwriterv2

import com.anthropic.client.AnthropicClient
import com.anthropic.core.http.StreamResponse
import com.anthropic.errors.RateLimitException
import com.anthropic.models.messages.MessageCreateParams
import com.anthropic.models.messages.RawMessageStreamEvent
import kotlin.jvm.optionals.getOrNull
import kotlin.streams.asSequence

/**
 * 추상화를 하는 이유(나는 추상화 싫어함)
 * 1. streaming response 핸들링
 * 2. exception 처리(특히 rate limit 처리)
 * 3. input token, output token, 처리 시간 같은 meta-data를 얻고 싶음
 * 를 다른 코드에서 신경쓰기 싫어서 여기서 정리를 함
 */

data class ClaudeResp(
    val inputTokens: Long,
    val outputTokens: Long,
    val message: String
)

fun AnthropicClient.requestStreaming(retry: Int = 1, paramBuildBlock: MessageCreateParams.Builder.() -> Unit): ClaudeResp {
    val param = MessageCreateParams.builder()
        .apply(paramBuildBlock)
        .build()

    return claudeRetry(retry) {
        val streamResponse: StreamResponse<RawMessageStreamEvent> = this.messages().createStreaming(param)
        streamResponse.handleStream()
    }
}

fun AnthropicClient.requestStreaming(param: MessageCreateParams, retry: Int = 1): ClaudeResp {
    return claudeRetry(retry) {
        val streamResponse: StreamResponse<RawMessageStreamEvent> = this.messages().createStreaming(param)
        streamResponse.handleStream()
    }
}

internal fun StreamResponse<RawMessageStreamEvent>.handleStream(): ClaudeResp {
    var inputToken = 0L
    var outputToken = 0L
    var model = ""
    val responseBuilder = StringBuilder()

    this.stream().asSequence().forEach { event ->
        when {
            event.isStart() -> {
                inputToken = event.asStart().message().usage().inputTokens()
                model = event.asStart().message().model().asString()
            }
            event.isContentBlockDelta() ->
                event.asContentBlockDelta().delta().text().getOrNull()!!.text()
                    .also { responseBuilder.append(it) }
                    .let { print(it) }
            event.isDelta() ->
                outputToken = event.asDelta().usage().outputTokens()
        }
    }
    return ClaudeResp(inputToken, outputToken, responseBuilder.toString())
}

internal inline fun <reified T> claudeRetry(retry: Int, block: () -> T): T {
    for (tryCnt in 1..retry) {
        val result = runCatching { block() }
        result.onSuccess { return it }
        result.onFailure { exception: Throwable ->
            if (exception !is RateLimitException) throw exception
        }
    }
    throw Exception("exceed max retry")
}
