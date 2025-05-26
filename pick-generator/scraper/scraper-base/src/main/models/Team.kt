package io.gitp.sbpick.pickgenerator.scraper.scrapebase.models

sealed interface BaseballTeam {
    val spojoyCode: String
    val naverSportsCode: String

    fun enumName(): String

    companion object {
        val allEntries: List<BaseballTeam> = BaseballTeam::class.sealedSubclasses.flatMap { subclass -> subclass.java.enumConstants.toList() }.map { it as BaseballTeam }

        fun findByNaverSportsCode(code: String): BaseballTeam {
            return allEntries.find { it.naverSportsCode == code } ?: throw IllegalArgumentException("can't find BaseballTeam enum entry by '${code}'")
        }

        fun findBySpojoyCode(code: String): BaseballTeam {
            return allEntries.find { it.spojoyCode == code } ?: throw IllegalArgumentException("can't find BaseballTeam enum entry by '${code}'")
        }
    }


    enum class NPBTeam(override val naverSportsCode: String, override val spojoyCode: String) : BaseballTeam {
        NIPPONHAM("NH", "니혼햄"),
        YOMIURI("YO", "요미우리"),
        YOKOHAMA("YK", "요코하마"),
        HANSHIN("HS", "한신"),
        CHUNICHI("JN", "주니치"),
        HIROSHIMA("HI", "히로시마"),
        YAKULT("YA", "야쿠르트"),
        RAKUTEN("RT", "라쿠텐"),
        SEIBU("SE", "세이부"),
        SOFTBANK("SF", "소프트뱅크"),
        ORIX("OX", "오릭스"),
        CHIBA_LOTTE_MARINES("JL", "지바롯데");

        override fun enumName(): String = this.name

        companion object {
            fun findByAnyCode(code: String): BaseballTeam =
                entries.find { it.naverSportsCode == code || it.spojoyCode == code } ?: throw IllegalArgumentException("can't find NPBTeam enum entry by '${code}'")
        }
    }


    enum class KBOTeam(override val naverSportsCode: String, override val spojoyCode: String) : BaseballTeam {
        SAMSUNG("SS", "삼성"),
        KIWOOM("WO", "키움"),
        DOOSAN("OB", "두산"),
        LG("LG", "LG"),
        KIA("HT", "KIA"),
        LOTTE("LT", "롯데"),
        SSG("SK", "SSG"),
        HANWHA("HH", "한화"),
        NC("NC", "NC"),
        KT("KT", "kt");


        override fun enumName(): String = this.name

        companion object {
            fun findByAnyCode(code: String): KBOTeam =
                values().find { it.naverSportsCode == code || it.spojoyCode == code } ?: throw IllegalArgumentException("can't find KBOTeam enum entry by '${code}'")
        }
    }

    enum class MLBTeam(override val naverSportsCode: String, override val spojoyCode: String) : BaseballTeam {
        MINNESOTA("MN", "미네소타"),
        BALTIMORE("BA", "볼티모어"),
        CINCINNATI("CI", "신시내티"),
        MISSOURI("SL", "세인트루이스"), // 주의: MO와 SL 모두 세인트루이스(St. Louis)로 매핑됨
        TEXAS("TE", "텍사스"),
        BOSTON("BO", "보스턴"),
        CHICAGO_WHITE_SOX("CW", "시카고W"),
        KANSAS_CITY("KC", "캔자스시티"),
        TORONTO("TO", "토론토"),
        ANAHEIM("AN", "LA에인절스"),
        ATLANTA("AT", "애틀랜타"),
        TAMPA_BAY("TB", "탬파베이"),
        PHILADELPHIA("PH", "필라델피아"),
        COLORADO("CO", "콜로라도"),
        DETROIT("DE", "디트로이트"),
        FLORIDA("FL", "마이애미"), // 주의: FL도 마이애미(Miami)로 매핑됨, 중복 코드
        MIAMI("MI", "밀워키"), // 주의: MI 코드가 밀워키(Milwaukee)로 잘못 매핑될 수 있음
        CHICAGO_CUBS("CC", "시카고컵스"),
        NEW_YORK_METS("NM", "뉴욕메츠"),
        PITTSBURGH("PI", "피츠버그"),
        SAN_FRANCISCO("SF", "샌프란시스코"),
        HOUSTON("HO", "휴스턴"),
        CLEVELAND("CL", "클리블랜드"), // 주의: 현재 팀명은 Guardians로 변경됨
        SAN_DIEGO("SD", "샌디에이고"),
        SEATTLE("SE", "시애틀"),
        OAKLAND("OA", "애슬레틱스"),
        LOS_ANGELES_DODGERS("LA", "LA다저스"),
        ARIZONA("AZ", "애리조나"),
        NEW_YORK_YANKEES("NY", "뉴욕양키스"),
        WASHINGTON("MO", "워싱턴");

        override fun enumName(): String = this.name

        companion object {
            fun findByAnyCode(code: String): MLBTeam =
                values().find { it.naverSportsCode == code || it.spojoyCode == code } ?: throw IllegalArgumentException("can't find MLBTeam enum entry by '${code}'")
        }
    }


}

enum class K1Team(val teamName: String) {
    JEONBUK("전북"),
    DAEJEON("대전"),
    GIMCHEON("김천"),
    POHANG("포항"),
    ANYANG("안양"),
    JEJU("제주"),
    SUWON_FC("수원FC"),
    SEOUL("서울"),
    DAEGU("대구"),
    ULSAN("울산"),
    GANGWON("강원"),
    GWANGJU("광주");

    companion object {
        fun fromTeamName(teamName: String): K1Team {
            return values().find { it.teamName == teamName } ?: throw IllegalArgumentException("can't find K1Team enum entry by '${teamName}'")
        }
    }
}