package io.gitp.sbpick.pickgenerator.scraper.scrapebase.models

sealed interface League {
    val sportsName: String
    val leagueName: String


    companion object {
        private val allEntries: List<League> = League::class.sealedSubclasses.flatMap { subclass -> subclass.java.enumConstants.toList() }.map { it as League }


        fun findByName(sportsName: String, leagueName: String): League? {
            return allEntries.find { it.sportsName == sportsName && it.leagueName == leagueName }
        }
    }

    enum class Basketball(override val leagueName: String, override val sportsName: String = "basketball") : League {
        CBA("CBA"),
    }

    enum class Hockey(override val leagueName: String, override val sportsName: String = "hockey") : League {
        NHL("NHL"), KHL("KHL"),
    }

    enum class Baseball(override val leagueName: String, override val sportsName: String = "baseball") : League {
        KBO("KBO"), MLB("MLB"), NPB("NPB"),
    }
}