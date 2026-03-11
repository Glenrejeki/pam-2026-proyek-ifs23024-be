package org.sonic.repositories

import org.sonic.entities.Tweet

interface ITweetRepository {
    suspend fun getTimeline(userId: String, followingIds: List<String>, page: Int, limit: Int): List<Tweet>
    suspend fun getByUserId(userId: String, page: Int, limit: Int): List<Tweet>
    suspend fun getById(tweetId: String): Tweet?
    suspend fun getReplies(tweetId: String, page: Int, limit: Int): List<Tweet>
    suspend fun search(query: String, page: Int, limit: Int): List<Tweet>
    suspend fun searchByHashtag(tag: String, page: Int, limit: Int): List<Tweet>
    suspend fun create(tweet: Tweet): String
    suspend fun update(userId: String, tweetId: String, newTweet: Tweet): Boolean
    suspend fun delete(userId: String, tweetId: String): Boolean
    suspend fun incrementLikes(tweetId: String): Boolean
    suspend fun decrementLikes(tweetId: String): Boolean
    suspend fun incrementRetweets(tweetId: String): Boolean
    suspend fun decrementRetweets(tweetId: String): Boolean
    suspend fun incrementReplies(tweetId: String): Boolean
    suspend fun incrementQuotes(tweetId: String): Boolean
    suspend fun decrementQuotes(tweetId: String): Boolean
}