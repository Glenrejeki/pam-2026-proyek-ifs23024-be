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

    Database.connect(
        url = "jdbc:postgresql://$dbHost:$dbPort/$dbName",
        user = dbUser,
        password = dbPassword
    )

    transaction {
        SchemaUtils.createMissingTablesAndColumns(
            UserTable, TweetTable, LikeTable, BookmarkTable,
            FollowTable, HashtagTable, TweetHashtagTable, RefreshTokenTable,
        )
    }
}