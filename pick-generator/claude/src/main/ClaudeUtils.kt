package io.gitp.pickgenerator.claude

import com.anthropic.client.AnthropicClient
import com.anthropic.core.http.StreamResponse
import com.anthropic.errors.RateLimitException
import com.anthropic.errors.SseException
import com.anthropic.models.messages.Message
import com.anthropic.models.messages.MessageCreateParams
import com.anthropic.models.messages.RawMessageStreamEvent
import io.gitp.pickgenerator.claude.models.ClaudeResp
import kotlinx.coroutines.future.asDeferred
import java.util.concurrent.CompletableFuture
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

suspend fun AnthropicClient.requestAsync(
    retry: Int = 3,
    sleepTime: Duration = 30.seconds,
    paramBuildBlock: MessageCreateParams.Builder.() -> Unit
): ClaudeResp {
    val param = MessageCreateParams.builder()
        .apply(paramBuildBlock)
        .build()

    return claudeRetry(retry, sleepTime) {
        val respFuture: CompletableFuture<Message> = this.async().messages().create(param)
        val resp = respFuture.asDeferred().await()

        ClaudeResp(
            inputTokens = resp.usage().inputTokens().toUInt(),
            outputTokens = resp.usage().outputTokens().toUInt(),
            message = resp.content().map { it.text().getOrNull()!!.text() }.joinToString("\n")
        )
    }
}

fun AnthropicClient.requestStreaming(
    retry: Int = 3,
    sleepTime: Duration = 30.seconds,
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

internal inline fun <reified T> claudeRetry(retry: Int, sleepTime: Duration = 30.seconds, block: () -> T): T {
    for (tryCnt in 1..retry) {
        val result = runCatching { block() }
        result.onSuccess { return it }
        result.onFailure { exception: Throwable ->
            if (exception !is RateLimitException && exception !is SseException) throw exception
            logger.warn("got ${exception} gonna sleep ${sleepTime}sec")
            Thread.sleep(sleepTime.toJavaDuration()) // delay를 써야하지만... 스크래핑 -> llm 쿼리 에서 후자가 병목점임으로 그냥 Thread.sleep
        }
    }
    throw Exception("exceed max retry")
}
