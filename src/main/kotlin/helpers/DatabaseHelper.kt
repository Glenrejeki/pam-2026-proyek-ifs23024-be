package org.sonic.helpers

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.sonic.tables.*

fun Application.configureDatabases() {
    val dbHost = environment.config.property("ktor.database.host").getString()
    val dbPort = environment.config.property("ktor.database.port").getString()
    val dbName = environment.config.property("ktor.database.name").getString()
    val dbUser = environment.config.property("ktor.database.user").getString()
    val dbPassword = environment.config.property("ktor.database.password").getString()

    val url = "jdbc:postgresql://$dbHost:$dbPort/$dbName"
    log.info("Connecting to database: $url as user: $dbUser")

    try {
        Database.connect(url = url, user = dbUser, password = dbPassword)
        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                UserTable, TweetTable, LikeTable, BookmarkTable,
                FollowTable, HashtagTable, TweetHashtagTable, RefreshTokenTable,
            )
        }
        log.info("Database connected successfully!")
    } catch (e: Exception) {
        log.error("Database connection failed: ${e.message}")
    }
}