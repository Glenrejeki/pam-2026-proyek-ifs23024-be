package org.sonic.repositories

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.sonic.dao.TweetDAO
import org.sonic.entities.Tweet
import org.sonic.helpers.suspendTransaction
import org.sonic.helpers.tweetDAOToModel
import org.sonic.tables.TweetTable
import java.util.UUID

class TweetRepository(private val baseUrl: String) : ITweetRepository {

    override suspend fun getTimeline(userId: String, followingIds: List<String>, page: Int, limit: Int): List<Tweet> = suspendTransaction {
        val allIds = (followingIds + userId).map { UUID.fromString(it) }
        TweetDAO.find { (TweetTable.userId inList allIds) and TweetTable.replyToId.isNull() }
            .orderBy(TweetTable.createdAt to SortOrder.DESC)
            .limit(limit).offset(((page - 1) * limit).toLong())  // ✅ Fix
            .map { tweetDAOToModel(it, baseUrl) }
    }

    override suspend fun getByUserId(userId: String, page: Int, limit: Int): List<Tweet> = suspendTransaction {
        TweetDAO.find { TweetTable.userId eq UUID.fromString(userId) }
            .orderBy(TweetTable.createdAt to SortOrder.DESC)
            .limit(limit).offset(((page - 1) * limit).toLong())  // ✅ Fix
            .map { tweetDAOToModel(it, baseUrl) }
    }

    override suspend fun getById(tweetId: String): Tweet? = suspendTransaction {
        TweetDAO.find { TweetTable.id eq UUID.fromString(tweetId) }
            .limit(1).map { tweetDAOToModel(it, baseUrl) }.firstOrNull()
    }

    override suspend fun getReplies(tweetId: String, page: Int, limit: Int): List<Tweet> = suspendTransaction {
        TweetDAO.find { TweetTable.replyToId eq UUID.fromString(tweetId) }
            .orderBy(TweetTable.createdAt to SortOrder.ASC)
            .limit(limit).offset(((page - 1) * limit).toLong())  // ✅ Fix
            .map { tweetDAOToModel(it, baseUrl) }
    }

    override suspend fun search(query: String, page: Int, limit: Int): List<Tweet> = suspendTransaction {
        val keyword = "%${query.lowercase()}%"
        TweetDAO.find { TweetTable.content.lowerCase() like keyword }
            .orderBy(TweetTable.createdAt to SortOrder.DESC)
            .limit(limit).offset(((page - 1) * limit).toLong())  // ✅ Fix
            .map { tweetDAOToModel(it, baseUrl) }
    }

    override suspend fun searchByHashtag(tag: String, page: Int, limit: Int): List<Tweet> = suspendTransaction {
        val keyword = "%#${tag.lowercase()}%"
        TweetDAO.find { TweetTable.content.lowerCase() like keyword }
            .orderBy(TweetTable.createdAt to SortOrder.DESC)
            .limit(limit).offset(((page - 1) * limit).toLong())  // ✅ Fix
            .map { tweetDAOToModel(it, baseUrl) }
    }

    override suspend fun create(tweet: Tweet): String = suspendTransaction {
        TweetDAO.new {
            userId = UUID.fromString(tweet.userId); content = tweet.content
            imageUrl = tweet.imageUrl
            retweetOfId = tweet.retweetOfId?.let { UUID.fromString(it) }
            quoteOfId = tweet.quoteOfId?.let { UUID.fromString(it) }
            replyToId = tweet.replyToId?.let { UUID.fromString(it) }
            language = tweet.language; createdAt = tweet.createdAt; updatedAt = tweet.updatedAt
        }.id.value.toString()
    }

    override suspend fun update(userId: String, tweetId: String, newTweet: Tweet): Boolean = suspendTransaction {
        val dao = TweetDAO.find {
            (TweetTable.id eq UUID.fromString(tweetId)) and (TweetTable.userId eq UUID.fromString(userId))
        }.limit(1).firstOrNull() ?: return@suspendTransaction false
        dao.content = newTweet.content; dao.imageUrl = newTweet.imageUrl; dao.updatedAt = newTweet.updatedAt
        true
    }

    override suspend fun delete(userId: String, tweetId: String): Boolean = suspendTransaction {
        TweetTable.deleteWhere {
            (TweetTable.id eq UUID.fromString(tweetId)) and (TweetTable.userId eq UUID.fromString(userId))
        } >= 1
    }

    override suspend fun incrementLikes(tweetId: String): Boolean = suspendTransaction {
        TweetTable.update({ TweetTable.id eq UUID.fromString(tweetId) }) {
            with(SqlExpressionBuilder) { it.update(TweetTable.likesCount, TweetTable.likesCount + 1) }
        } >= 1
    }

    override suspend fun decrementLikes(tweetId: String): Boolean = suspendTransaction {
        TweetTable.update({ TweetTable.id eq UUID.fromString(tweetId) }) {
            with(SqlExpressionBuilder) { it.update(TweetTable.likesCount, TweetTable.likesCount - 1) }
        } >= 1
    }

    override suspend fun incrementRetweets(tweetId: String): Boolean = suspendTransaction {
        TweetTable.update({ TweetTable.id eq UUID.fromString(tweetId) }) {
            with(SqlExpressionBuilder) { it.update(TweetTable.retweetsCount, TweetTable.retweetsCount + 1) }
        } >= 1
    }

    override suspend fun decrementRetweets(tweetId: String): Boolean = suspendTransaction {
        TweetTable.update({ TweetTable.id eq UUID.fromString(tweetId) }) {
            with(SqlExpressionBuilder) { it.update(TweetTable.retweetsCount, TweetTable.retweetsCount - 1) }
        } >= 1
    }

    override suspend fun incrementReplies(tweetId: String): Boolean = suspendTransaction {
        TweetTable.update({ TweetTable.id eq UUID.fromString(tweetId) }) {
            with(SqlExpressionBuilder) { it.update(TweetTable.repliesCount, TweetTable.repliesCount + 1) }
        } >= 1
    }

    override suspend fun incrementQuotes(tweetId: String): Boolean = suspendTransaction {
        TweetTable.update({ TweetTable.id eq UUID.fromString(tweetId) }) {
            with(SqlExpressionBuilder) { it.update(TweetTable.quotesCount, TweetTable.quotesCount + 1) }
        } >= 1
    }

    override suspend fun decrementQuotes(tweetId: String): Boolean = suspendTransaction {
        TweetTable.update({ TweetTable.id eq UUID.fromString(tweetId) }) {
            with(SqlExpressionBuilder) { it.update(TweetTable.quotesCount, TweetTable.quotesCount - 1) }
        } >= 1
    }
}