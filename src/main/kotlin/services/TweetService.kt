package org.sonic.services

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import org.sonic.data.*
import org.sonic.entities.Tweet
import org.sonic.entities.TweetAuthor
import org.sonic.helpers.*
import org.sonic.repositories.*
import java.io.File
import java.util.UUID

class TweetService(
    private val userRepo: IUserRepository,
    private val tweetRepo: ITweetRepository,
    private val likeRepo: ILikeRepository,
    private val bookmarkRepo: IBookmarkRepository,
    private val followRepo: IFollowRepository,
    private val hashtagRepo: IHashtagRepository,
    private val baseUrl: String,
) {
    private suspend fun enrichTweet(tweet: Tweet, authUserId: String?): Tweet {
        val author = userRepo.getById(tweet.userId)
        var enriched = tweet.copy(
            author = if (author != null) TweetAuthor(
                id = author.id, name = author.name, username = author.username,
                urlPhoto = author.urlPhoto, isVerified = author.isVerified
            ) else null
        )
        if (authUserId != null) {
            enriched = enriched.copy(
                isLiked = likeRepo.isLiked(authUserId, tweet.id),
                isBookmarked = bookmarkRepo.isBookmarked(authUserId, tweet.id),
            )
        }
        if (enriched.retweetOfId != null) {
            tweetRepo.getById(enriched.retweetOfId!!)?.let { enriched = enriched.copy(retweetOf = it) }
        }
        if (enriched.quoteOfId != null) {
            tweetRepo.getById(enriched.quoteOfId!!)?.let { enriched = enriched.copy(quoteOf = it) }
        }
        return enriched
    }

    suspend fun getTimeline(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val limit = (call.request.queryParameters["limit"]?.toIntOrNull() ?: 20).coerceAtMost(50)
        val filter = call.request.queryParameters["filter"] ?: "all"

        val followingIds = followRepo.getFollowingIds(user.id)
        val ids = if (filter == "following") followingIds else followingIds
        val tweets = tweetRepo.getTimeline(user.id, ids, page, limit).map { enrichTweet(it, user.id) }

        call.respond(DataResponse("success", "Timeline berhasil diambil",
            TimelineData(tweets, page, limit)))
    }

    suspend fun getUserTweets(call: ApplicationCall) {
        val targetUserId = call.parameters["userId"] ?: throw AppException(400, "User ID tidak valid!")
        val authUserId = try { ServiceHelper.getAuthUserId(call) } catch (e: Exception) { null }
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val limit = (call.request.queryParameters["limit"]?.toIntOrNull() ?: 20).coerceAtMost(50)
        val tweets = tweetRepo.getByUserId(targetUserId, page, limit).map { enrichTweet(it, authUserId) }
        call.respond(DataResponse("success", "Tweet pengguna berhasil diambil",
            TimelineData(tweets, page, limit)))
    }

    suspend fun getById(call: ApplicationCall) {
        val tweetId = call.parameters["id"] ?: throw AppException(400, "Tweet ID tidak valid!")
        val authUserId = try { ServiceHelper.getAuthUserId(call) } catch (e: Exception) { null }
        val tweet = tweetRepo.getById(tweetId) ?: throw AppException(404, "Tweet tidak ditemukan!")
        val replies = tweetRepo.getReplies(tweetId, 1, 20).map { enrichTweet(it, authUserId) }
        call.respond(DataResponse("success", "Tweet berhasil diambil",
            TweetDetailData(enrichTweet(tweet, authUserId), replies)))
    }

    suspend fun post(call: ApplicationCall) {
        // Deteksi content type — jika multipart, delegasikan ke postWithImage
        if (call.request.contentType().match(ContentType.MultiPart.FormData)) {
            postWithImage(call)
            return
        }

        val user = ServiceHelper.getAuthUser(call, userRepo)
        val request = call.receive<TweetRequest>()
        request.userId = user.id

        if (request.retweetOfId == null) {
            val validator = ValidatorHelper(request.toMap())
            validator.required("content", "Konten tweet tidak boleh kosong")
            validator.validate()
        }

        val tweetId = tweetRepo.create(request.toEntity())
        request.replyToId?.let { tweetRepo.incrementReplies(it) }
        request.quoteOfId?.let { tweetRepo.incrementQuotes(it) }
        request.retweetOfId?.let { tweetRepo.incrementRetweets(it) }

        val hashtags = extractHashtags(request.content)
        if (hashtags.isNotEmpty()) {
            hashtagRepo.upsertHashtags(hashtags)
            hashtagRepo.linkToTweet(tweetId, hashtags)
        }
        call.respond(DataResponse("success", "Tweet berhasil diposting", TweetIdData(tweetId)))
    }

    suspend fun postWithImage(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)
        val request = TweetRequest(userId = user.id)

        call.receiveMultipart(formFieldLimit = 1024 * 1024 * 10).forEachPart { part ->
            when (part) {
                is PartData.FormItem -> when (part.name) {
                    "content" -> request.content = part.value
                    "replyToId" -> request.replyToId = part.value.ifBlank { null }
                    "quoteOfId" -> request.quoteOfId = part.value.ifBlank { null }
                    "retweetOfId" -> request.retweetOfId = part.value.ifBlank { null }
                    "language" -> request.language = part.value.ifBlank { "id" }
                }
                is PartData.FileItem -> {
                    val ext = part.originalFileName?.substringAfterLast('.', "")
                        ?.let { if (it.isNotEmpty()) ".$it" else "" } ?: ""
                    val filePath = "uploads/tweets/${UUID.randomUUID()}$ext"
                    File(filePath).also { it.parentFile.mkdirs() }.let { file ->
                        part.provider().copyAndClose(file.writeChannel())
                    }
                    request.imageUrl = filePath
                }
                else -> {}
            }
            part.dispose()
        }

        val tweetId = tweetRepo.create(request.toEntity())
        request.replyToId?.let { tweetRepo.incrementReplies(it) }
        request.quoteOfId?.let { tweetRepo.incrementQuotes(it) }
        request.retweetOfId?.let { tweetRepo.incrementRetweets(it) }

        val hashtags = extractHashtags(request.content)
        if (hashtags.isNotEmpty()) {
            hashtagRepo.upsertHashtags(hashtags)
            hashtagRepo.linkToTweet(tweetId, hashtags)
        }
        call.respond(DataResponse("success", "Tweet berhasil diposting", TweetIdData(tweetId)))
    }

    suspend fun put(call: ApplicationCall) {
        val tweetId = call.parameters["id"] ?: throw AppException(400, "Tweet ID tidak valid!")
        val user = ServiceHelper.getAuthUser(call, userRepo)

        // Deteksi content type untuk edit juga
        if (call.request.contentType().match(ContentType.MultiPart.FormData)) {
            val request = TweetRequest(userId = user.id)
            var keepExistingImage = true

            call.receiveMultipart(formFieldLimit = 1024 * 1024 * 10).forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> when (part.name) {
                        "content" -> request.content = part.value
                        "language" -> request.language = part.value.ifBlank { "id" }
                    }
                    is PartData.FileItem -> {
                        keepExistingImage = false
                        val ext = part.originalFileName?.substringAfterLast('.', "")
                            ?.let { if (it.isNotEmpty()) ".$it" else "" } ?: ""
                        val filePath = "uploads/tweets/${UUID.randomUUID()}$ext"
                        File(filePath).also { it.parentFile.mkdirs() }.let { file ->
                            part.provider().copyAndClose(file.writeChannel())
                        }
                        request.imageUrl = filePath
                    }
                    else -> {}
                }
                part.dispose()
            }

            val oldTweet = tweetRepo.getById(tweetId) ?: throw AppException(404, "Tweet tidak ditemukan!")
            if (oldTweet.userId != user.id) throw AppException(403, "Tidak berhak mengubah tweet ini!")

            request.userId = user.id
            val imageUrl = if (keepExistingImage) oldTweet.imageUrl else request.imageUrl
            tweetRepo.update(user.id, tweetId, request.toEntity().copy(imageUrl = imageUrl))

            hashtagRepo.unlinkFromTweet(tweetId)
            val hashtags = extractHashtags(request.content)
            if (hashtags.isNotEmpty()) {
                hashtagRepo.upsertHashtags(hashtags)
                hashtagRepo.linkToTweet(tweetId, hashtags)
            }
            call.respond(DataResponse("success", "Tweet berhasil diubah", null))
            return
        }

        val request = call.receive<TweetRequest>()
        val validator = ValidatorHelper(request.toMap())
        validator.required("content", "Konten tweet tidak boleh kosong")
        validator.validate()

        val oldTweet = tweetRepo.getById(tweetId) ?: throw AppException(404, "Tweet tidak ditemukan!")
        if (oldTweet.userId != user.id) throw AppException(403, "Tidak berhak mengubah tweet ini!")
        if (oldTweet.retweetOfId != null && oldTweet.quoteOfId == null)
            throw AppException(400, "Retweet murni tidak bisa diedit!")

        request.userId = user.id
        tweetRepo.update(user.id, tweetId, request.toEntity().copy(imageUrl = oldTweet.imageUrl))
        hashtagRepo.unlinkFromTweet(tweetId)
        val hashtags = extractHashtags(request.content)
        if (hashtags.isNotEmpty()) {
            hashtagRepo.upsertHashtags(hashtags)
            hashtagRepo.linkToTweet(tweetId, hashtags)
        }
        call.respond(DataResponse("success", "Tweet berhasil diubah", null))
    }

    suspend fun delete(call: ApplicationCall) {
        val tweetId = call.parameters["id"] ?: throw AppException(400, "Tweet ID tidak valid!")
        val user = ServiceHelper.getAuthUser(call, userRepo)
        val tweet = tweetRepo.getById(tweetId) ?: throw AppException(404, "Tweet tidak ditemukan!")
        if (tweet.userId != user.id) throw AppException(403, "Tidak berhak menghapus tweet ini!")

        tweet.quoteOfId?.let { tweetRepo.decrementQuotes(it) }
        tweet.retweetOfId?.let { tweetRepo.decrementRetweets(it) }
        hashtagRepo.unlinkFromTweet(tweetId)

        if (!tweetRepo.delete(user.id, tweetId)) throw AppException(400, "Gagal menghapus tweet!")
        tweet.imageUrl?.let { File(it).takeIf { f -> f.exists() }?.delete() }
        call.respond(DataResponse("success", "Tweet berhasil dihapus", null))
    }

    suspend fun like(call: ApplicationCall) {
        val tweetId = call.parameters["id"] ?: throw AppException(400, "Tweet ID tidak valid!")
        val user = ServiceHelper.getAuthUser(call, userRepo)
        tweetRepo.getById(tweetId) ?: throw AppException(404, "Tweet tidak ditemukan!")

        if (likeRepo.isLiked(user.id, tweetId)) {
            likeRepo.unlike(user.id, tweetId)
            tweetRepo.decrementLikes(tweetId)
            call.respond(DataResponse("success", "Tweet di-unlike", TweetLikedData(false)))
        } else {
            likeRepo.like(user.id, tweetId)
            tweetRepo.incrementLikes(tweetId)
            call.respond(DataResponse("success", "Tweet di-like", TweetLikedData(true)))
        }
    }

    suspend fun bookmark(call: ApplicationCall) {
        val tweetId = call.parameters["id"] ?: throw AppException(400, "Tweet ID tidak valid!")
        val user = ServiceHelper.getAuthUser(call, userRepo)
        tweetRepo.getById(tweetId) ?: throw AppException(404, "Tweet tidak ditemukan!")

        if (bookmarkRepo.isBookmarked(user.id, tweetId)) {
            bookmarkRepo.removeBookmark(user.id, tweetId)
            call.respond(DataResponse("success", "Markah dihapus", TweetBookmarkedData(false)))
        } else {
            bookmarkRepo.bookmark(user.id, tweetId)
            call.respond(DataResponse("success", "Tweet ditandai", TweetBookmarkedData(true)))
        }
    }

    suspend fun getMyBookmarks(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val limit = (call.request.queryParameters["limit"]?.toIntOrNull() ?: 20).coerceAtMost(50)
        val tweetIds = bookmarkRepo.getBookmarkedTweetIdsList(user.id, page, limit)
        val tweets = tweetIds.mapNotNull { tweetRepo.getById(it) }.map { enrichTweet(it, user.id) }
        call.respond(DataResponse("success", "Markah berhasil diambil",
            TimelineData(tweets, page, limit)))
    }

    suspend fun search(call: ApplicationCall) {
        val query = call.request.queryParameters["q"] ?: throw AppException(400, "Query tidak boleh kosong!")
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val limit = (call.request.queryParameters["limit"]?.toIntOrNull() ?: 20).coerceAtMost(50)
        val authUserId = try { ServiceHelper.getAuthUserId(call) } catch (e: Exception) { null }

        val tweets = if (query.startsWith("#")) {
            tweetRepo.searchByHashtag(query.removePrefix("#"), page, limit)
        } else {
            tweetRepo.search(query, page, limit)
        }.map { enrichTweet(it, authUserId) }

        call.respond(DataResponse("success", "Hasil pencarian",
            SearchTweetsData(tweets, query, page, limit)))
    }

    suspend fun getTweetImage(call: ApplicationCall) {
        val tweetId = call.parameters["id"] ?: throw AppException(400, "Tweet ID tidak valid!")
        val tweet = tweetRepo.getById(tweetId) ?: throw AppException(404, "Tweet tidak ditemukan!")
        val path = tweet.imageUrl ?: throw AppException(404, "Tweet tidak memiliki gambar!")
        val file = File(path)
        if (!file.exists()) throw AppException(404, "Gambar tidak tersedia!")
        call.respondFile(file)
    }
}