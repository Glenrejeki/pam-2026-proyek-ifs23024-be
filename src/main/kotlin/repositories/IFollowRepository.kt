package org.sonic.repositories

interface IFollowRepository {
    suspend fun isFollowing(followerId: String, followingId: String): Boolean
    suspend fun follow(followerId: String, followingId: String): Boolean
    suspend fun unfollow(followerId: String, followingId: String): Boolean
    suspend fun getFollowingIds(userId: String): List<String>
    suspend fun getFollowers(userId: String, page: Int, limit: Int): List<String>
    suspend fun getFollowing(userId: String, page: Int, limit: Int): List<String>
}