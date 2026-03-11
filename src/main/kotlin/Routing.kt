package org.sonic

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import org.sonic.data.AppException
import org.sonic.data.ErrorResponse
import org.sonic.helpers.JWTConstants
import org.sonic.helpers.parseMessageToMap
import org.sonic.services.*

fun Application.configureRouting() {
    val authService: AuthService by inject()
    val tweetService: TweetService by inject()
    val userService: UserService by inject()
    val searchService: SearchService by inject()

    install(StatusPages) {
        exception<AppException> { call, cause ->
            val dataMap = parseMessageToMap(cause.message)
            call.respond(HttpStatusCode.fromValue(cause.code),
                ErrorResponse(
                    status = "fail",
                    message = if (dataMap.isEmpty()) cause.message else "Data yang dikirimkan tidak valid!",
                    data = if (dataMap.isEmpty()) null else dataMap.toString()
                )
            )
        }
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError,
                ErrorResponse("error", cause.message ?: "Terjadi kesalahan internal", ""))
        }
    }

    routing {
        get("/") { call.respondText("🚀 Sonic API is running!") }

        // Auth
        route("/auth") {
            post("/register") { authService.postRegister(call) }
            post("/login") { authService.postLogin(call) }
            post("/refresh-token") { authService.postRefreshToken(call) }
            post("/logout") { authService.postLogout(call) }
        }

        // Public Search & Trending
        route("/search") {
            get("/tweets") { tweetService.search(call) }         // ?q=kata atau ?q=#hashtag
            get("/users") { userService.searchUsers(call) }      // ?q=username
            get("/trending") { searchService.getTrending(call) } // ?limit=10
        }

        // Public Profiles
        route("/users") {
            get("/{username}") { userService.getUserProfile(call) }
            get("/{userId}/followers") { userService.getFollowers(call) }
            get("/{userId}/following") { userService.getFollowing(call) }
            get("/{userId}/tweets") { tweetService.getUserTweets(call) }
        }

        // Public tweet detail
        route("/tweets") {
            get("/{id}") { tweetService.getById(call) }
        }

        // Static images
        route("/images") {
            get("/users/{id}") { userService.getPhoto(call) }
            get("/tweets/{id}") { tweetService.getTweetImage(call) }
        }

        // Authenticated routes
        authenticate(JWTConstants.NAME) {

            route("/users/me") {
                get { userService.getMe(call) }
                put { userService.putMe(call) }
                put("/profile") { userService.putMyProfile(call) }
                put("/photo") { userService.putMyPhoto(call) }
                put("/header") { userService.putMyHeaderPhoto(call) }
                put("/password") { userService.putMyPassword(call) }
            }

            route("/users/{userId}") {
                post("/follow") { userService.follow(call) }
                delete("/follow") { userService.unfollow(call) }
            }

            route("/tweets") {
                get { tweetService.getTimeline(call) }              // ?page=1&limit=20&filter=all|following
                post { tweetService.post(call) }                    // JSON tweet
                post("/media") { tweetService.postWithImage(call) } // Multipart tweet+gambar
                put("/{id}") { tweetService.put(call) }
                delete("/{id}") { tweetService.delete(call) }
                post("/{id}/like") { tweetService.like(call) }
                post("/{id}/bookmark") { tweetService.bookmark(call) }
            }

            route("/bookmarks") {
                get { tweetService.getMyBookmarks(call) }
            }
        }
    }
}