package org.sonic.repositories

import org.sonic.entities.Hashtag

interface IHashtagRepository {
    suspend fun getTrending(limit: Int): List<Hashtag>
    suspend fun upsertHashtags(tags: List<String>)
    suspend fun linkToTweet(tweetId: String, tags: List<String>)
    suspend fun unlinkFromTweet(tweetId: String)
}