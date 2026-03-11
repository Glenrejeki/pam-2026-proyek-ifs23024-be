package org.sonic.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.sonic.entities.Tweet

@Serializable
data class TweetRequest(
    var userId: String = "",
    var content: String = "",
    var imageUrl: String? = null,
    var retweetOfId: String? = null,
    var quoteOfId: String? = null,
    var replyToId: String? = null,
    var language: String = "id",
) {
    fun toMap() = mapOf(
        "userId" to userId, "content" to content,
        "retweetOfId" to retweetOfId, "quoteOfId" to quoteOfId
    )

    fun toEntity() = Tweet(
        userId = userId, content = content, imageUrl = imageUrl,
        retweetOfId = retweetOfId, quoteOfId = quoteOfId,
        replyToId = replyToId, language = language,
        updatedAt = Clock.System.now()
    )
}