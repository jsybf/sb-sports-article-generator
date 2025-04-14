import io.gitp.llmarticlewriter.database.*
import model.League
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals

class HockeyRepoTest {
    private val db = Database.connect(
        "jdbc:sqlite::memory:",
        "org.sqlite.JDBC",
        databaseConfig = DatabaseConfig { sqlLogger = ExposedLogger }
    )

    private val hockeyRepo = HockeyRepo(db)

    @Test
    fun `test insert scenario`(): Unit = transaction(db) {
        SchemaUtils.create(HockeyMatchTbl, HockeyScrapedTbl, HockeyArticleTbl)
        val match1 = HockeyMatchDto(null, "team1", "team2", LocalDateTime.now(), League.KHL, "www.sample1.com")
        val scraped1 = HockeyScrapedPageDto(null, "summary1", "oneXTwoBet1", "overUnderBet1")
        val article = HockeyArticleDto(null, "article1", 0, 0)
        val matchId = hockeyRepo.insertHockeyMatch(match1)
        hockeyRepo.insertHockeyScrapedPage(matchId, scraped1)
        hockeyRepo.insertHockeyArticle(matchId, article)
    }

    @Test
    fun `test  scenario`(): Unit = transaction(db) {
        SchemaUtils.create(HockeyMatchTbl, HockeyScrapedTbl, HockeyArticleTbl)

        val match1 = HockeyMatchDto(null, "team1", "team2", LocalDateTime.now(), League.KHL, "www.sample1.com")
        val scraped1 = HockeyScrapedPageDto(null, "summary1", "oneXTwoBet1", "overUnderBet1")
        val article1 = HockeyArticleDto(null, "article1", 0, 0)
        val match2 = HockeyMatchDto(null, "team2", "team3", LocalDateTime.now(), League.KHL, "www.sample2.com")
        val scraped2 = HockeyScrapedPageDto(null, "summary2", "oneXTwoBet2", "overUnderBet2")

        val matchId1 = hockeyRepo.insertHockeyMatch(match1)
        hockeyRepo.insertHockeyScrapedPage(matchId1, scraped1)
        hockeyRepo.insertHockeyArticle(matchId1, article1)

        val matchId2 = hockeyRepo.insertHockeyMatch(match1)
        hockeyRepo.insertHockeyScrapedPage(matchId2, scraped2)
        assertEquals(
            1,
            hockeyRepo.findNotGeneratedMatches().count()
        )
    }
}