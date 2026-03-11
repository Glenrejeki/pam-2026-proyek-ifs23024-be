package org.sonic.helpers

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.sonic.data.AppException
import org.sonic.entities.User
import org.sonic.repositories.IUserRepository

object ServiceHelper {
    suspend fun getAuthUser(call: ApplicationCall, userRepository: IUserRepository): User {
        val principal = call.principal<JWTPrincipal>()
            ?: throw AppException(401, "Unauthorized")
        val userId = principal.payload.getClaim("userId").asString()
            ?: throw AppException(401, "Token tidak valid")
        return userRepository.getById(userId) ?: throw AppException(401, "User tidak valid")
    }

    fun getAuthUserId(call: ApplicationCall): String {
        val principal = call.principal<JWTPrincipal>()
            ?: throw AppException(401, "Unauthorized")
        return principal.payload.getClaim("userId").asString()
            ?: throw AppException(401, "Token tidak valid")
    }
}