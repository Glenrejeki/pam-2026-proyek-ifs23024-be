package org.sonic.repositories

import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.sonic.dao.HashtagDAO
import org.sonic.dao.TweetHashtagDAO
import org.sonic.entities.Hashtag
import org.sonic.helpers.hashtagDAOToModel
import org.sonic.helpers.suspendTransaction
import org.sonic.tables.HashtagTable
import org.sonic.tables.TweetHashtagTable
import java.util.UUID

class HashtagRepository : IHashtagRepository {

    override suspend fun getTrending(limit: Int): List<Hashtag> = suspendTransaction {
        HashtagDAO.all()
            .orderBy(HashtagTable.count to SortOrder.DESC)
            .limit(limit)
            .map { hashtagDAOToModel(it) }
    }

    override suspend fun upsertHashtags(tags: List<String>): Unit = suspendTransaction {
        tags.forEach { tag ->
            val existing = HashtagDAO.find { HashtagTable.tag eq tag }.limit(1).firstOrNull()
            if (existing != null) {
                existing.count = existing.count + 1
                existing.updatedAt = Clock.System.now()
            } else {
                HashtagDAO.new { this.tag = tag; this.count = 1; this.updatedAt = Clock.System.now() }
            }
        }
    }

    override suspend fun linkToTweet(tweetId: String, tags: List<String>): Unit = suspendTransaction {
        tags.forEach { tag ->
            val hashtag = HashtagDAO.find { HashtagTable.tag eq tag }.limit(1).firstOrNull() ?: return@forEach
            TweetHashtagDAO.new {
                this.tweetId = UUID.fromString(tweetId)
                this.hashtagId = hashtag.id.value
            }
        }
    }

    override suspend fun unlinkFromTweet(tweetId: String): Unit = suspendTransaction {
        TweetHashtagTable.deleteWhere { TweetHashtagTable.tweetId eq UUID.fromString(tweetId) }
    }
}