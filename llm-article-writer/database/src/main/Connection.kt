package io.gitp.llmarticlewriter.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig

// "jdbc:sqlite::memory:",
fun getDBConnection(jdbcUrl: String) = Database.connect(
    jdbcUrl,
    "org.sqlite.JDBC",
    databaseConfig = DatabaseConfig { sqlLogger = ExposedLogger }
)
