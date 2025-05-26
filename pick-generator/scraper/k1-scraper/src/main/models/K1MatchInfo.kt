package io.gitp.sbpick.pickgenerator.scraper.k1scraper.models

import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.MatchInfo
import java.time.LocalDateTime

data class K1MatchInfo(
    override val awayTeam: String,
    override val homeTeam: String,
    override val matchAt: LocalDateTime,
    override val league: League,
    val flashscoreDetailPageUrl: String
) : MatchInfo
