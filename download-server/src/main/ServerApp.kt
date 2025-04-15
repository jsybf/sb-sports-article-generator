package io.gitp.downloadserver

import io.gitp.llmarticlewriter.database.SportsRepository
import io.gitp.llmarticlewriter.database.getSqliteConn
import io.ktor.server.application.*
import io.ktor.server.routing.*
import java.nio.file.Path


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.routeModule() {
    val sqlitePath = environment.config.propertyOrNull("sqlite.path")?.getString().let { Path.of(it) } ?: throw Exception("specify sqlite.path in application.conf")
    val zipExporter = PicksZipExporter(SportsRepository(getSqliteConn(sqlitePath)))
    routing {
        downloadRoute(zipExporter)
    }
}