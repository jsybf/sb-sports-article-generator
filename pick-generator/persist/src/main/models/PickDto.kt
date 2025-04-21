package io.gitp.sbpick.pickgenerator.database.models

import io.gitp.sbpick.pickgenerator.database.PickEntity


internal fun PickEntity.toPickDto(): PickDto = PickDto(
    id = this.id.value,
    sportsMatchId = this.sportsMatch.id.value,
    content = this.content,
    inputTokens = this.inputTokens,
    outputTokens = this.outputTokens
)

data class PickDto(
    val id: UInt?,
    val sportsMatchId: UInt?,
    val content: String,
    val inputTokens: UInt,
    val outputTokens: UInt
)


