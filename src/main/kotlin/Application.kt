package org.sonic

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.github.cdimascio.dotenv.dotenv
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.sonic.helpers.JWTConstants
import org.sonic.helpers.configureDatabases
import org.sonic.helpers.configureStaticFiles
import org.sonic.module.appModule

fun main(args: Array<String>) {
    val dotenv = dotenv { ignoreIfMissing = true }
    dotenv.entries().forEach { entry ->
        System.setProperty(entry.key, entry.value)
    }
    EngineMain.main(args)
}

fun Application.module() {
    val jwtSecret = environment.config.property("ktor.jwt.secret").getString()

    install(Authentication) {
        jwt(JWTConstants.NAME) {
            realm = JWTConstants.REALM
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(JWTConstants.ISSUER)
                    .withAudience(JWTConstants.AUDIENCE)
                    .build()
            )
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asString()
                if (!userId.isNullOrBlank()) JWTPrincipal(credential.payload) else null
            }
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("status" to "error", "message" to "Token tidak valid atau sudah expired")
                )
            }
        }
    }

    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.Accept)
        allowHeader(HttpHeaders.Origin)
        allowCredentials = true
        exposeHeader(HttpHeaders.ContentDisposition)
    }

    install(ContentNegotiation) {
        json(Json {
            explicitNulls = false
            prettyPrint = true
            ignoreUnknownKeys = true
        })
    }

    install(Koin) {
        slf4jLogger()
        modules(appModule(this@module))
    }

    configureDatabases()
    configureStaticFiles()
    configureRouting()
}