package models

import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.League
import org.junit.Test
import kotlin.test.assertEquals

class LeagueEnumTest {
    @Test
    fun `find by name test`() {
        assertEquals(
            League.Basketball.CBA,
            League.findByName("basketball", "CBA")
        )
        assertEquals(
            League.Hockey.NHL,
            League.findByName("hockey", "NHL")
        )
        assertEquals(
            null,
            League.findByName("hockey", "???")
        )
        assertEquals(
            null,
            League.findByName("???", "KBO")
        )
    }

    fun `get name test`() {
        assertEquals("baseball", League.Baseball.KBO.sportsName)
        assertEquals("KBO", League.Baseball.KBO.leagueName)
    }
}