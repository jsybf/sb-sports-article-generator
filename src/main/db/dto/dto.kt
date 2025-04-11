package db.dto

import scrape.hockey.HockeyPage

object HockeyDto{
    data class MatchPageSetDto(
        val id: Int,
        val matchPageSet: HockeyPage.MatchPageSet
    )
}