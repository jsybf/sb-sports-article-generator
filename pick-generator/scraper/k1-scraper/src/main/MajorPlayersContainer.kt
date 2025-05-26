package io.gitp.sbpick.pickgenerator.scraper.k1scraper

import io.gitp.sbpick.pickgenerator.scraper.scrapebase.models.K1Team
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private fun readResourceFile(path: String): String = object {}::class.java.getResource(path)?.readText() ?: throw Exception("can't find ${path} in resource")

@Serializable
internal data class MajorPlayers(
    @SerialName("player_name")
    val playerName: String,
    @SerialName("position")
    val position: String,
    @SerialName("description")
    val description: String
)

internal object MajorPlayersContainer {
    private val players: Map<K1Team, List<MajorPlayers>> = readResourceFile("/major_players.json")
        .let { Json.decodeFromString<Map<String, List<MajorPlayers>>>(it) }
        .let { it.mapKeys { entry -> K1Team.fromTeamName(entry.key) } }

    fun findByTeamName(teamName: K1Team): List<MajorPlayers> = players[teamName]!!
}


fun main() {
    MajorPlayersContainer.findByTeamName(K1Team.JEJU).also { println(it) }
}

