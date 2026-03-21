package org.sonic.services

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.sonic.data.DataResponse
import org.sonic.repositories.IHashtagRepository
import org.sonic.repositories.IUserRepository

class SearchService(
    private val hashtagRepo: IHashtagRepository,
    private val userRepo: IUserRepository,
) {
    suspend fun getTrending(call: ApplicationCall) {
        val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
        val trending = hashtagRepo.getTrending(limit.coerceAtMost(50))
        call.respond(DataResponse("success", "Trending hashtag", trending))
    }
}