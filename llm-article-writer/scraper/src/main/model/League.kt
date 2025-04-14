package model

sealed interface Leaguee {
    enum class BasketBall(val matchListPageUrl: String) : Leaguee {
        CBA("https://www.flashscore.co.kr/basketball/china/cba/fixtures/")
    }

    enum class Hockey(val matchListPageUrl: String) : Leaguee {
        NHL("https://www.flashscore.co.kr/hockey/usa/nhl/fixtures/"),
        KHL("https://www.flashscore.co.kr/hockey/russia/khl/fixtures/")
    }

}