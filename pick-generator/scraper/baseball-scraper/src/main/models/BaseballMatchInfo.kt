package io.gitp.sbpick.pickgenerator.scraper.baseballscraper.models

import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.MatchInfo
import kotlinx.serialization.json.JsonObject
import java.time.LocalDateTime

data class BaseballMatchInfo(
    override val awayTeam: String,
    override val homeTeam: String,
    override val matchAt: LocalDateTime,
    override val league: League.Baseball,
) : MatchInfo
