package io.gitp.pickgenerator.claude

import com.anthropic.client.AnthropicClient
import com.anthropic.core.http.StreamResponse
import com.anthropic.errors.RateLimitException
import com.anthropic.errors.SseException
import com.anthropic.models.messages.MessageCreateParams
import com.anthropic.models.messages.RawMessageStreamEvent
import io.gitp.pickgenerator.claude.models.ClaudeResp
import java.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration


fun AnthropicClient.requestStreaming(
    retry: Int = 3,
    sleepTime: Duration = 30.seconds.toJavaDuration(),
    paramBuildBlock: MessageCreateParams.Builder.() -> Unit
): ClaudeResp {
    val param = MessageCreateParams.builder()
        .apply(paramBuildBlock)
        .build()

    return claudeRetry(retry, sleepTime) {
        val streamResponse: StreamResponse<RawMessageStreamEvent> = this.messages().createStreaming(param)
        streamResponse.handleStream()
            .also { claudeResp ->
                logger.info("inputTokens:{} outputTokens:{}", claudeResp.inputTokens, claudeResp.outputTokens)
            }
    }
}

internal inline fun <reified T> claudeRetry(retry: Int, sleepTime: Duration = 30.seconds.toJavaDuration(), block: () -> T): T {
    for (tryCnt in 1..retry) {
        val result = runCatching { block() }
        result.onSuccess { return it }
        result.onFailure { exception: Throwable ->
            if (exception !is RateLimitException && exception !is SseException) throw exception
            logger.warn("got ${exception} gonna sleep ${sleepTime}sec")
            Thread.sleep(sleepTime)
        }
    }
    throw Exception("exceed max retry")
}
