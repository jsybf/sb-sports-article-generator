package io.gitp.pickgenerator.claude.models

data class ClaudeResp(
    val inputTokens: UInt,
    val outputTokens: UInt,
    val message: String
)
