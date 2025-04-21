package browser

import io.gitp.sbpick.pickgenerator.scraper.scrapebase.browser.PlaywrightBrowserPool
import io.gitp.sbpick.pickgenerator.scraper.scrapebase.logger
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jsoup.nodes.Document
import kotlin.test.Test

class PlaywrightBrowserPoolExamplesTest {
    @Test
    fun `example 1`() = runBlocking {
        val browserPool = PlaywrightBrowserPool(3)

        val player1 = browserPool.doAndGetDocAsync {
            logger.info("requesting player1")
            navigate("http://www.spojoy.com/sportsinfo/infomation/club/player_info.spo?player_code=154396")
        }
        val player2 = browserPool.doAndGetDocAsync {
            logger.info("requesting player2")
            navigate("http://www.spojoy.com/sportsinfo/infomation/club/player_info.spo?player_code=154396")
        }
        val player3 = browserPool.doAndGetDocAsync {
            logger.info("requesting player3")
            navigate("http://www.spojoy.com/sportsinfo/infomation/club/player_info.spo?player_code=154396")
        }

        player1.await()
        player2.await()
        player3.await()
        browserPool.close()
    }

    @Test
    fun `example 2`() = runBlocking {
        val browserPool = PlaywrightBrowserPool(3)

        val job1 = launch {
            scrapePlayers("job1", browserPool)
        }
        val job2 = launch {
            scrapePlayers("job2", browserPool)
        }

        job1.join()
        job2.join()
        browserPool.close()
    }


    private suspend fun scrapePlayers(jobName: String, browserPool: PlaywrightBrowserPool) {
        val playerListPage = browserPool.doAndGetDocAsync {
            navigate("https://spodb.spojoy.com/team/?team_id=1060&pgTk=players")
        }
        logger.info("scraped playerListPage (jobName:${jobName})")
        val playerPageUrls = playerListPage.await().extractPlayerPageUrl()

        val asyncs = playerPageUrls.mapIndexed { idx, playerPageUrl ->
            logger.info("(jobName:${jobName} requestingPlayer:${idx})")
            browserPool.doAndGetDocAsync { navigate(playerPageUrl) }
        }

        asyncs.awaitAll()
    }

    private val extractPlayerCode = Regex("""[0-9]+""")
    private fun Document.extractPlayerPageUrl(): List<String> {
        return this
            .select("""tr[height="50"]""")
            .asIterable()
            .filter { element -> element.select("td:nth-child(4)").text() != "P" }
            .map { element -> element.select("""a[target="player"]""") }
            .map { element ->
                val onClickAttr = element.attr("onclick")
                val playerCode = extractPlayerCode.find(onClickAttr)!!.value.toInt()
                val playerPageUrl = "https://www.spojoy.com/sportsinfo/infomation/club/player_info.spo?player_code=${playerCode}"
                playerPageUrl
            }
            .distinct()
    }


}