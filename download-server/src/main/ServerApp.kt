package io.gitp.downloadserver

import io.gitp.sbpick.pickgenerator.database.repositories.PickRepository
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)


fun Application.routeModule() {
    val mysqlHost = environment.config.property("mysql.host").getString()
    val mysqlPort = environment.config.property("mysql.port").getString()
    val mysqlDatabase = environment.config.property("mysql.db").getString()
    val mysqlUser = environment.config.property("mysql.user").getString()
    val mysqlPassword = environment.config.property("mysql.pw").getString()

    val db = Database.connect(
        url = "jdbc:mysql://${mysqlHost}:${mysqlPort}/${mysqlDatabase}".also { println("jdbc_url: $it") },
        user = mysqlUser,
        password = mysqlPassword
    )

    val pickRepo = PickRepository(db)

    routing {
        downloadRoute(pickRepo)
    }
}