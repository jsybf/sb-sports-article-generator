import io.gitp.llmarticlewriter.database.*
import io.gitp.llmarticlewriter.scraper.model.League
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals

class SportsRepositoryTest {
    private val db = Database.connect(
        "jdbc:sqlite::memory:",
        "org.sqlite.JDBC",
        databaseConfig = DatabaseConfig { sqlLogger = ExposedLogger }
    )

    private val sportsRepository = SportsRepository(db)

    @Test
    fun `test insert scenario`(): Unit = transaction(db) {
        SchemaUtils.create(SportsMatchTable, FlashScoreScrapedTbl, ArticleTbl)
        val match1 = MatchInfoDto(null, "team1", "team2", LocalDateTime.now(), League.Hockey.NHL, "www.sample1.com")
        val scraped1 = HockeyScrapedPageDto(null, "summary1", "oneXTwoBet1", "overUnderBet1")
        val article = HockeyArticleDto(null, "article1", 0, 0)
        val matchId = sportsRepository.insertHockeyMatch(match1)
        sportsRepository.insertHockeyScrapedPage(matchId, scraped1)
        sportsRepository.insertHockeyArticle(matchId, article)
    }

    @Test
    fun `test  scenario`(): Unit = transaction(db) {
        SchemaUtils.create(SportsMatchTable, FlashScoreScrapedTbl, ArticleTbl)

        val match1 = MatchInfoDto(null, "team1", "team2", LocalDateTime.now(), League.Hockey.KHL, "www.sample1.com")
        val scraped1 = HockeyScrapedPageDto(null, "summary1", "oneXTwoBet1", "overUnderBet1")
        val article1 = HockeyArticleDto(null, "article1", 0, 0)
        val match2 = MatchInfoDto(null, "team2", "team3", LocalDateTime.now(), League.Hockey.KHL, "www.sample2.com")
        val scraped2 = HockeyScrapedPageDto(null, "summary2", "oneXTwoBet2", "overUnderBet2")

        val matchId1 = sportsRepository.insertHockeyMatch(match1)
        sportsRepository.insertHockeyScrapedPage(matchId1, scraped1)
        sportsRepository.insertHockeyArticle(matchId1, article1)

        val matchId2 = sportsRepository.insertHockeyMatch(match1)
        sportsRepository.insertHockeyScrapedPage(matchId2, scraped2)
        assertEquals(
            1,
            sportsRepository.findNotGeneratedMatches().count()
        )
    }
}