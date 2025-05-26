package io.gitp.sbpick.pickgenerator.scraper.scrapebase.models

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

    enum class Basketball(override val leagueName: String, val matchListPageUrl: String, override val sportsName: String = "basketball") : League {
        CBA("CBA", "https://www.flashscore.co.kr/basketball/china/cba/fixtures/"),
        WNBA("WNBA", "https://www.flashscore.co.kr/basketball/usa/wnba/fixtures/"),
    }

    enum class Hockey(override val leagueName: String, val matchListPageUrl: String, override val sportsName: String = "hockey") : League {
        NHL("NHL", "https://www.flashscore.co.kr/hockey/usa/nhl/fixtures/"),
        KHL("KHL", "https://www.flashscore.co.kr/hockey/russia/khl/fixtures/"),
        WORLD("세계선수권", "https://www.flashscore.co.kr/hockey/world/world-championship/fixtures/"),
        AHL("AHL", "https://www.flashscore.co.kr/hockey/usa/ahl/fixtures/"),
    }

    enum class Baseball(override val leagueName: String, val matchListPageUrl: (matchAt: LocalDate) -> String, override val sportsName: String = "baseball") : League {
        KBO("KBO", { matchAt -> "https://www.spojoy.com/live/?mct=baseball&sct=201&pgTk=&sel_date=${spojoyDateFormatter.format(matchAt)}" }),
        MLB("MLB", { matchAt -> "https://www.spojoy.com/live/?mct=baseball&sct=202&pgTk=&sel_date=${spojoyDateFormatter.format(matchAt)}" }),
        NPB("NPB", { matchAt -> "https://www.spojoy.com/live/?mct=baseball&sct=208&pgTk=&sel_date=${spojoyDateFormatter.format(matchAt)}" });

    }

    enum class KoreaSoccer(override val leagueName: String, val fixtureListPageUrl: String, override val sportsName: String = "korea-soccer") : League {
        K1("K1", "https://www.flashscore.co.kr/soccer/south-korea/k-league-1/fixtures/")
    }

    enum class MinorSoccer(override val leagueName: String, val fixtureListPageUrl: String, override val sportsName: String = "minor-soccer") : League {
        K2("K2", "https://www.flashscore.co.kr/soccer/south-korea/k-league-2/fixtures/"),
        K3("K3", "https://www.flashscore.co.kr/soccer/south-korea/k3-league/fixtures/"),
        WK("WK", "https://www.flashscore.co.kr/soccer/south-korea/wk-league-women/fixtures/")
    }
}

private val spojoyDateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
