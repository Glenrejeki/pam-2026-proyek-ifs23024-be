package org.sonic.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object UserTable : UUIDTable("users") {
    val name = varchar("name", 100)
    val username = varchar("username", 50).uniqueIndex()
    val email = varchar("email", 100).uniqueIndex()
    val password = varchar("password", 255)
    val bio = text("bio").nullable()
    val photo = varchar("photo", 255).nullable()
    val headerPhoto = varchar("header_photo", 255).nullable()
    val location = varchar("location", 100).nullable()
    val website = varchar("website", 255).nullable()
    val isVerified = bool("is_verified").default(false)
    val followersCount = integer("followers_count").default(0)
    val followingCount = integer("following_count").default(0)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}