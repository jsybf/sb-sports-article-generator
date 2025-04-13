package scrape.hockey

import java.time.LocalDateTime

fun HockeyPage.SummaryPage.parseTeam(): Pair<String, String> {
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

fun HockeyPage.SummaryPage.parseStartDateTime(): LocalDateTime {

}