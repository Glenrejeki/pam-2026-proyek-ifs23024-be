package org.sonic.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object HashtagTable : UUIDTable("hashtags") {
    val tag = varchar("tag", 100).uniqueIndex()
    val count = integer("count").default(0)
    val updatedAt = timestamp("updated_at")
}

object TweetHashtagTable : UUIDTable("tweet_hashtags") {
    val tweetId = uuid("tweet_id").references(TweetTable.id)
    val hashtagId = uuid("hashtag_id").references(HashtagTable.id)
}