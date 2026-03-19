package org.sonic.data

import kotlinx.serialization.Serializable
import org.sonic.entities.Hashtag
import org.sonic.entities.Tweet

// Auth
@Serializable
data class AuthRegisterData(val userId: String)

@Serializable
data class AuthTokenData(val authToken: String, val refreshToken: String)

// Tweet
@Serializable
data class TweetIdData(val tweetId: String)

@Serializable
data class TweetLikedData(val liked: Boolean)

@Serializable
data class TweetBookmarkedData(val bookmarked: Boolean)

@Serializable
data class TimelineData(val tweets: List<Tweet>, val page: Int, val limit: Int)

@Serializable
data class TweetDetailData(val tweet: Tweet, val replies: List<Tweet>)

// User
@Serializable
data class UserData(val user: UserResponse)

@Serializable
data class UsersData(val users: List<UserResponse>, val page: Int, val limit: Int)

// Search
@Serializable
data class SearchTweetsData(val tweets: List<Tweet>, val query: String, val page: Int, val limit: Int)

@Serializable
data class SearchUsersData(val users: List<UserResponse>, val query: String, val page: Int, val limit: Int)

// Trending — pakai Hashtag langsung, sudah @Serializable
@Serializable
data class TrendingData(val trending: List<Hashtag>)