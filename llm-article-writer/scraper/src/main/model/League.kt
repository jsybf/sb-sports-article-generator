package io.gitp.llmarticlewriter.scraper.model

sealed interface League {
    val sportsName: String
    val leagueName: String

    enum class BasketBall(val matchListPageUrl: String) : League {
        CBA("https://www.flashscore.co.kr/basketball/china/cba/fixtures/") {
            override val leagueName = this.name
        };

        override val sportsName: String = "basketball"
    }

    enum class Hockey(val matchListPageUrl: String) : League {
        NHL("https://www.flashscore.co.kr/hockey/usa/nhl/fixtures/") {
            override val leagueName: String = this.name
        },
        KHL("https://www.flashscore.co.kr/hockey/russia/khl/fixtures/") {
            override val leagueName: String = this.name
        };

        override val sportsName: String = "hockey"
    }

    companion object {
        private val allLeagues: Set<League> = (BasketBall.entries + Hockey.entries).toSet()
        fun ofName(sportName: String, leagueName: String): League = allLeagues.find { it.sportsName == sportName && it.leagueName == leagueName }
            ?: throw Exception("cant find League Enum with sportName:$sportName leagueName:$leagueName")
    }

}