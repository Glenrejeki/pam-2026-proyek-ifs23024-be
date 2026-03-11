package org.sonic.entities

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Tweet(
    var id: String = UUID.randomUUID().toString(),
    var userId: String,
    var content: String,
    var imageUrl: String? = null,
    var urlImage: String = "",
    var retweetOfId: String? = null,
    var quoteOfId: String? = null,
    var replyToId: String? = null,
    var language: String = "id",
    var likesCount: Int = 0,
    var retweetsCount: Int = 0,
    var repliesCount: Int = 0,
    var quotesCount: Int = 0,
    var author: TweetAuthor? = null,
    var retweetOf: Tweet? = null,
    var quoteOf: Tweet? = null,
    var isLiked: Boolean = false,
    var isRetweeted: Boolean = false,
    var isBookmarked: Boolean = false,
    @Contextual val createdAt: Instant = Clock.System.now(),
    @Contextual var updatedAt: Instant = Clock.System.now(),
)

@Serializable
data class TweetAuthor(
    val id: String,
    val name: String,
    val username: String,
    val urlPhoto: String,
    val isVerified: Boolean,
)