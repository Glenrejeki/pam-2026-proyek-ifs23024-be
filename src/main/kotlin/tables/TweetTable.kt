package org.sonic.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object TweetTable : UUIDTable("tweets") {
    val userId = uuid("user_id").references(UserTable.id)
    val content = text("content")
    val imageUrl = text("image_url").nullable()
    val retweetOfId = uuid("retweet_of_id").nullable()
    val quoteOfId = uuid("quote_of_id").nullable()
    val replyToId = uuid("reply_to_id").nullable()
    val language = varchar("language", 10).default("id")
    val likesCount = integer("likes_count").default(0)
    val retweetsCount = integer("retweets_count").default(0)
    val repliesCount = integer("replies_count").default(0)
    val quotesCount = integer("quotes_count").default(0)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}