package io.gitp.llmarticlewriter.scraper.model.pages.hockey

import kotlinx.serialization.json.JsonObject
import org.jsoup.nodes.Document
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class HockeyMatchPage(
    val doc: Document
) {

    fun extractMatchInfo(): JsonObject = with(HockeyMatchPageParser) { parseMatchInfo() }

    /**
     * @return (homeTeam, awayTeam)
     */
    fun extractTeams(): Pair<String, String> {
        val homeTeam = doc
            .select("#detail > div.duelParticipant > div.duelParticipant__home > div.participant__participantNameWrapper > div.participant__participantName.participant__overflow > a")
            .first()!!
            .text()

        val awayTeam = doc
            .select("#detail > div.duelParticipant > div.duelParticipant__away > div.participant__participantNameWrapper > div.participant__participantName.participant__overflow > a")
            .first()!!
            .text()

        return Pair(homeTeam, awayTeam)
    }

    fun extractStartAt(): LocalDateTime {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

        val matchDateTimeStr = doc
            .select("#detail > div.duelParticipant > div.duelParticipant__startTime > div")
            .first()!!
            .text()

        return LocalDateTime.parse(matchDateTimeStr, formatter)
    }
}