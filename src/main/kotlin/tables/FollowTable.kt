package org.sonic.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object FollowTable : UUIDTable("follows") {
    val followerId = uuid("follower_id").references(UserTable.id)
    val followingId = uuid("following_id").references(UserTable.id)
    val createdAt = timestamp("created_at")
}