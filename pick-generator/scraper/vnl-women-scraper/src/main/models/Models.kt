package io.gitp.sbpick.pickgenerator.scraper.vnlwommenscraper.models

import kotlinx.serialization.Serializable


@Serializable
internal data class AudienceVote(
    val homeTeamWinPercentage: String,
    val awayTeamWinPercentage: String,
    val totalVotes: String // Total votes: 3.5k 이런형식 어차피 llm한테 던져줄 것이기 때문에 int로 파싱 하지 않겠다.
)

@Serializable
internal data class MatchStatistics(
    val homeTeamPoints: Int,
    val awayTeamPoints: Int,
    val homeTeamServicePoints: String,
    val awayTeamServicePoints: String,
    val homeTeamReceiverPoints: String,
    val awayTeamReceiverPoints: String,
    val homeTeamAces: Int,
    val awayTeamAces: Int,
    val homeTeamMaxPointsInRow: Int,
    val awayTeamMaxPointsInRow: Int,
    val homeTeamserviceErrors: Int,
    val awayTeamserviceErrors: Int
)

@Serializable
internal data class Setscore(
    val homeTeamScore: Int,
    val awayTeamScore: Int
)

@Serializable
internal data class SofascoreScrapedResult(
    val audienceVote: AudienceVote,
    val matchStatistics: MatchStatistics,
    val setScore: Map<Int, Setscore>
) {
    companion object {
        fun from(audienceVote: AudienceVote, matchStatistics: MatchStatistics, setScoreList: List<Setscore>) =  SofascoreScrapedResult(
            audienceVote = audienceVote,
            matchStatistics = matchStatistics,
            setScore= setScoreList.mapIndexed { index, setScore -> Pair(index, setScore) }.toMap()
        )
    }
}
