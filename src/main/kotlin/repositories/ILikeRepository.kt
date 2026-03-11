package org.sonic.repositories

interface ILikeRepository {
    suspend fun isLiked(userId: String, tweetId: String): Boolean
    suspend fun like(userId: String, tweetId: String): Boolean
    suspend fun unlike(userId: String, tweetId: String): Boolean
    suspend fun getLikedTweetIds(userId: String, tweetIds: List<String>): Set<String>
}