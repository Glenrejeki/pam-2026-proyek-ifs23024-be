package org.sonic.repositories

interface IBookmarkRepository {
    suspend fun isBookmarked(userId: String, tweetId: String): Boolean
    suspend fun bookmark(userId: String, tweetId: String): Boolean
    suspend fun removeBookmark(userId: String, tweetId: String): Boolean
    suspend fun getBookmarkedTweetIds(userId: String, tweetIds: List<String>): Set<String>
    suspend fun getBookmarkedTweetIdsList(userId: String, page: Int, limit: Int): List<String>
}