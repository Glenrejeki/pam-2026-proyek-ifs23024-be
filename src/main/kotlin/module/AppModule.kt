package org.sonic.module

import io.ktor.server.application.*
import org.koin.dsl.module
import org.sonic.repositories.BookmarkRepository
import org.sonic.repositories.FollowRepository
import org.sonic.repositories.HashtagRepository
import org.sonic.repositories.IBookmarkRepository
import org.sonic.repositories.IFollowRepository
import org.sonic.repositories.IHashtagRepository
import org.sonic.repositories.ILikeRepository
import org.sonic.repositories.IRefreshTokenRepository
import org.sonic.repositories.ITweetRepository
import org.sonic.repositories.IUserRepository
import org.sonic.repositories.LikeRepository
import org.sonic.repositories.RefreshTokenRepository
import org.sonic.repositories.TweetRepository
import org.sonic.repositories.UserRepository
import org.sonic.services.AuthService
import org.sonic.services.SearchService
import org.sonic.services.TweetService
import org.sonic.services.UserService

fun appModule(application: Application) = module {
    val baseUrl = application.environment.config
        .property("ktor.app.baseUrl").getString().trimEnd('/')
    val jwtSecret = application.environment.config
        .property("ktor.jwt.secret").getString()

    single<IUserRepository> { UserRepository(baseUrl) }
    single<ITweetRepository> { TweetRepository(baseUrl) }
    single<ILikeRepository> { LikeRepository() }
    single<IBookmarkRepository> { BookmarkRepository() }
    single<IFollowRepository> { FollowRepository() }
    single<IHashtagRepository> { HashtagRepository() }
    single<IRefreshTokenRepository> { RefreshTokenRepository() }

    single { AuthService(jwtSecret, get(), get()) }
    single { TweetService(get(), get(), get(), get(), get(), get(), baseUrl) }
    single { UserService(get(), get(), get()) }
    single { SearchService(get(), get()) }
}