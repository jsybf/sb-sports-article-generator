package io.gitp.llmarticlewriter.database.dto

import scrape.hockey.HockeyPage

object HockeyDto {
    data class MatchPageSetDto(
        val id: Int,
        val matchPageSet: HockeyPage.MatchPageSet
    )
}