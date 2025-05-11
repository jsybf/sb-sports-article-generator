package io.gitp.sbpick.pickgenerator.scraper.scrapebase.models

import java.net.URI
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed interface League {
    val sportsName: String
    val leagueName: String


    companion object {
        val allEntries: List<League> = League::class.sealedSubclasses.flatMap { subclass -> subclass.java.enumConstants.toList() }.map { it as League }


        fun findByName(sportsName: String, leagueName: String): League? {
            return allEntries.find { it.sportsName == sportsName && it.leagueName == leagueName }
        }
    }

    enum class Basketball(override val leagueName: String, val matchListPageUrl: URI, override val sportsName: String = "basketball") : League {
        CBA("CBA", URI("https://www.flashscore.co.kr/basketball/china/cba/fixtures/")),
    }

    enum class Hockey(override val leagueName: String, val matchListPageUrl: URI, override val sportsName: String = "hockey") : League {
        NHL("NHL", URI("https://www.flashscore.co.kr/hockey/usa/nhl/fixtures/")),
        KHL("KHL", URI("https://www.flashscore.co.kr/hockey/russia/khl/fixtures/")),
    }

    enum class Baseball(override val leagueName: String, val matchListPageUrl: (matchAt: LocalDate) -> String, override val sportsName: String = "baseball") : League {
        KBO("KBO", { matchAt -> "https://www.spojoy.com/live/?mct=baseball&sct=201&pgTk=&sel_date=${spojoyDateFormatter.format(matchAt)}" }),
        MLB("MLB", { matchAt -> "https://www.spojoy.com/live/?mct=baseball&sct=202&pgTk=&sel_date=${spojoyDateFormatter.format(matchAt)}" }),
        NPB("NPB", { matchAt -> "https://www.spojoy.com/live/?mct=baseball&sct=208&pgTk=&sel_date=${spojoyDateFormatter.format(matchAt)}" });

    }
}

private val spojoyDateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
