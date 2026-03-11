package org.sonic.helpers

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.sonic.dao.HashtagDAO
import org.sonic.dao.RefreshTokenDAO
import org.sonic.dao.TweetDAO
import org.sonic.dao.UserDAO
import org.sonic.entities.Hashtag
import org.sonic.entities.RefreshToken
import org.sonic.entities.Tweet
import org.sonic.entities.User

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

fun buildImageUrl(baseUrl: String, path: String?): String {
    if (path == null) return "$baseUrl/static/defaults/user.png"
    val relativePath = path.removePrefix("uploads/")
    return "$baseUrl/static/$relativePath"
}

fun userDAOToModel(dao: UserDAO, baseUrl: String) = User(
    id = dao.id.value.toString(),
    name = dao.name,
    username = dao.username,
    email = dao.email,
    password = dao.password,
    bio = dao.bio,
    photo = dao.photo,
    urlPhoto = buildImageUrl(baseUrl, dao.photo),
    headerPhoto = dao.headerPhoto,
    urlHeaderPhoto = buildImageUrl(baseUrl, dao.headerPhoto),
    location = dao.location,
    website = dao.website,
    isVerified = dao.isVerified,
    followersCount = dao.followersCount,
    followingCount = dao.followingCount,
    createdAt = dao.createdAt,
    updatedAt = dao.updatedAt
)

fun tweetDAOToModel(dao: TweetDAO, baseUrl: String) = Tweet(
    id = dao.id.value.toString(),
    userId = dao.userId.toString(),
    content = dao.content,
    imageUrl = dao.imageUrl,
    urlImage = if (dao.imageUrl != null) buildImageUrl(baseUrl, dao.imageUrl) else "",
    retweetOfId = dao.retweetOfId?.toString(),
    quoteOfId = dao.quoteOfId?.toString(),
    replyToId = dao.replyToId?.toString(),
    language = dao.language,
    likesCount = dao.likesCount,
    retweetsCount = dao.retweetsCount,
    repliesCount = dao.repliesCount,
    quotesCount = dao.quotesCount,
    createdAt = dao.createdAt,
    updatedAt = dao.updatedAt
)

fun refreshTokenDAOToModel(dao: RefreshTokenDAO) = RefreshToken(
    id = dao.id.value.toString(),
    userId = dao.userId.toString(),
    refreshToken = dao.refreshToken,
    authToken = dao.authToken,
    createdAt = dao.createdAt,
)

fun hashtagDAOToModel(dao: HashtagDAO) = Hashtag(
    id = dao.id.value.toString(),
    tag = dao.tag,
    count = dao.count,
    updatedAt = dao.updatedAt,
)