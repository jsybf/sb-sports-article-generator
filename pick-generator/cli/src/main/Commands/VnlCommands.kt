package io.gitp.sbpick.pickgenerator.pickgenerator.Commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import io.gitp.sbpick.pickgenerator.database.repositories.VnlWomenRepository
import org.jetbrains.exposed.sql.Database

class ScrapePrevMatchesCommand: CliktCommand("scrap-prev") {
    private val mysqlHost by option("--mysql_host", envvar = "SB_PICK_MYSQL_HOST")
    private val mysqlPort by option("--mysql_port", envvar = "SB_PICK_MYSQL_PORT")
    private val mysqlUser by option("--mysql_user", envvar = "SB_PICK_MYSQL_USER")
    private val mysqlPassword by option("--mysql_pw", envvar = "SB_PICK_MYSQL_PW")
    private val mysqlDatabase by option("--mysql_db", envvar = "SB_PICK_MYSQL_DB")

    private val claudeApiKey by option("--claude_api_key", envvar = "SB_PICK_CLAUDE_API_KEY")

    private val db = Database.connect(
        url = "jdbc:mysql://${mysqlHost!!}:${mysqlPort!!}/${mysqlDatabase!!}".also { println("jdbc_url: ${it}") },
        user = mysqlUser!!,
        password = mysqlPassword!!
    )

    private val vnlWomenRepository =  VnlWomenRepository(db)
    override fun run() {


    }
}

class