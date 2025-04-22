package io.gitp.sbpick.pickgenerator.scraper.hockeyscraper.models

import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.MatchInfo
import java.time.LocalDateTime

data class HockeyMatchInfo(
    override val awayTeam: String,
    override val homeTeam: String,
    override val matchAt: LocalDateTime,
    override val league: League.Hockey,
) : MatchInfo

