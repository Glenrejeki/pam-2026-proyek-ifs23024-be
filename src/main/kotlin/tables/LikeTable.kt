package org.sonic.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object LikeTable : UUIDTable("likes") {
    val userId = uuid("user_id").references(UserTable.id)
    val tweetId = uuid("tweet_id").references(TweetTable.id)
    val createdAt = timestamp("created_at")
}