package io.gitp.pickgenerator.claude

import com.anthropic.core.http.StreamResponse
import com.anthropic.models.messages.RawMessageStreamEvent
import io.gitp.pickgenerator.claude.models.ClaudeResp
import kotlin.jvm.optionals.getOrNull
import kotlin.streams.asSequence

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
                event.asContentBlockDelta().delta().text().getOrNull()!!.text().also { responseBuilder.append(it) }
            event.isDelta() ->
                outputToken = event.asDelta().usage().outputTokens()
        }
    }
    return ClaudeResp(inputToken.toUInt(), outputToken.toUInt(), responseBuilder.toString())
}
