package io.gitp.llmarticlewriter.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Path

// "jdbc:sqlite::memory:",
fun getSqliteConn(sqlitePath: Path) = getSqliteConn("jdbc:sqlite:${sqlitePath.normalize()}")

fun getSqliteConn(jdbcUrl: String) =
    Database
        .connect(
            url = jdbcUrl,
            driver = "org.sqlite.JDBC",
            databaseConfig = DatabaseConfig { sqlLogger = ExposedLogger }
        )
        .also { db ->
            transaction(db) { SchemaUtils.create(SportsMatchTable, FlashScoreScrapedTbl, ArticleTbl) }
        }
