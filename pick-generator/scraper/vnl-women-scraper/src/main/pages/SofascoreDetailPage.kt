package io.gitp.sbpick.pickgenerator.scraper.vnlwommenscraper.pages

import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.vnlwommenscraper.models.AudienceVote
import io.gitp.sbpick.pickgenerator.scraper.vnlwommenscraper.models.MatchStatistics
import io.gitp.sbpick.pickgenerator.scraper.vnlwommenscraper.models.Setscore
import org.jsoup.nodes.Document

internal data class SofaDetailPage(val doc: Document)

internal suspend fun PlaywrightBrowserPool.scrapeSofascoreDetailPage(url: String): SofaDetailPage = this
        .doAndGetDocAsync {
            navigate("https://www.sofascore.com/volleyball/match/turkiye-france/dJcsFJc#id:13315035")
            repeat(10) { this.mouse().wheel(0.0, 100.0) } // 마우스 스크롤을 해야지 통계 정보드이 로딩이 된다...
        }
        .await()
        .let { SofaDetailPage(it) }


internal fun SofaDetailPage.extractAudienceVotes(): Result<AudienceVote> = runCatching {
    val audienceVoteContainer = this@extractAudienceVotes.doc
        .select("""#__next > main > div.fresnel-container.fresnel-greaterThanOrEqual-mdMin.fresnel-\:r1\: > div.w_100\%.max-w_\[1440px\].mx_auto.md\:px_md.min-h_\[100vh\].pb_md > div.d_flex.flex-wrap_wrap.gap_xl.mdOnly\:gap_md > div.d_flex.flex-d_column.mdDown\:flex-sh_1.mdDown\:flex-b_100\%.gap_md.w_\[0px\].flex-g_1 > div:nth-child(2)""")


    val (homeTeamPercentage, awayTeamPercentage) = audienceVoteContainer
        .select(".Text.gHLcGU")
        .map { it.text() }

    val totalVotes = audienceVoteContainer.selectFirst(".px_lg span")!!.text()

    return@runCatching AudienceVote(
        homeTeamWinPercentage = homeTeamPercentage,
        awayTeamWinPercentage = awayTeamPercentage,
        totalVotes = totalVotes
    )
}

internal fun SofaDetailPage.extractSetScores(): Result<List<Setscore>> = runCatching {
    val tableElement =  this@extractSetScores.doc.select(
        """#__next > main > div.fresnel-container.fresnel-greaterThanOrEqual-mdMin.fresnel-\:r1\: > div.w_100\%.max-w_\[1440px\].mx_auto.md\:px_md.min-h_\[100vh\].pb_md > div.d_flex.flex-wrap_wrap.gap_xl.mdOnly\:gap_md > div.d_flex.flex-d_column.mdDown\:flex-sh_1.mdDown\:flex-b_100\%.gap_md.w_\[0px\].flex-g_1 > div:nth-child(1) > div.d_flex.flex-d_column.gap_sm.p_sm > div:nth-child(4)"""
    )
    val homeSetscoreList =  tableElement.select("tbody > tr:nth-child(1) span").map { it.text() }.filter { it !="" }.map { it.toInt() }
    val awaySetscoreList =  tableElement.select("tbody > tr:nth-child(2) span").map { it.text() }.filter { it !="" }.map { it.toInt() }

    homeSetscoreList.zip(awaySetscoreList).map { (homeScore, awayScore) -> Setscore(homeScore, awayScore) }
}


internal  fun SofaDetailPage.extractStatisticsBox(): Result<MatchStatistics> = runCatching {
    val statisticsElements = this@extractStatisticsBox.doc.select(".Box.Flex.heNsMA.bnpRyo")

    val (homeTeamPoints, awayTeamPoints) = run {
        val homeTeamPoints = statisticsElements[0].selectFirst(".iQnHnj span")!!.text().toInt()
        val awayTeamPoints = statisticsElements[0].selectFirst(".fdyVPU span")!!.text().toInt()

        Pair(homeTeamPoints, awayTeamPoints)
    }

    val (homeTeamServicePoints, awayTeamServicePoints) = run {
        val homeTeamServicePoints = statisticsElements[1].selectFirst(".iQnHnj span")!!.text()
        val awayTeamServicePoints = statisticsElements[1].selectFirst(".fdyVPU span")!!.text()

        Pair(homeTeamServicePoints, awayTeamServicePoints)
    }

    val (homeTeamReceiverPoints, awayTeamReceiverPoints) = run {
        val homeTeamReceiverPoints = statisticsElements[2].selectFirst(".iQnHnj span")!!.text()
        val awayTeamReceiverPoints = statisticsElements[2].selectFirst(".fdyVPU span")!!.text()

        Pair(homeTeamReceiverPoints, awayTeamReceiverPoints)
    }

    val (homeTeamAces, awayTeamAces) = run {
        val homeTeamAces = statisticsElements[3].selectFirst(".iQnHnj span")!!.text().toInt()
        val awayTeamAces = statisticsElements[3].selectFirst(".fdyVPU span")!!.text().toInt()

        Pair(homeTeamAces, awayTeamAces)
    }

    val (homeTeamMaxPointsInRow, awayTeamMaxPointsInRow) = run {
        val homeTeamMaxPointsInRow = statisticsElements[4].selectFirst(".iQnHnj span")!!.text().toInt()
        val awayTeamMaxPointsInRow = statisticsElements[4].selectFirst(".fdyVPU span")!!.text().toInt()

        Pair(homeTeamMaxPointsInRow, awayTeamMaxPointsInRow)
    }

    val (homeTeamserviceErrors, awayTeamserviceErrors) = run {
        val homeTeamserviceErrors = statisticsElements[5].selectFirst(".iQnHnj span")!!.text().toInt()
        val awayTeamserviceErrors = statisticsElements[5].selectFirst(".fdyVPU span")!!.text().toInt()

        Pair(homeTeamserviceErrors, awayTeamserviceErrors)
    }

    return@runCatching  MatchStatistics(
        homeTeamPoints,
        awayTeamPoints,
        homeTeamServicePoints,
        awayTeamServicePoints,
        homeTeamReceiverPoints,
        awayTeamReceiverPoints,
        homeTeamAces,
        awayTeamAces,
        homeTeamMaxPointsInRow,
        awayTeamMaxPointsInRow,
        homeTeamserviceErrors,
        awayTeamserviceErrors
    )
}

