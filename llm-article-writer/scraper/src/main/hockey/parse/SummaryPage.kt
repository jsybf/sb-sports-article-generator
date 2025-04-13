package scrape.hockey.parse

import kotlinx.serialization.json.JsonArray
import scrape.hockey.HockeyPage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * (홈팀, 원정팀) 페어로 반환
 */
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

/**
 * 경기 시작 날짜를 html에서 파싱
 */
fun HockeyPage.SummaryPage.parseStartDateTime(): LocalDateTime {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    val matchDateTimeStr = doc
        .select("#detail > div.duelParticipant > div.duelParticipant__startTime > div")
        .first()!!
        .text()

    return LocalDateTime.parse(matchDateTimeStr, formatter)
}

fun HockeyPage.SummaryPage.parseAbsencePlayerUrlList(): List<String> = doc
    .select(".lf__isReversed a")
    .map { "https://www.flashscore.co.kr" + it.attribute("href")!!.value }