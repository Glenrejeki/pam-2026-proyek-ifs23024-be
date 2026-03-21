package org.sonic.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.sonic.data.*
import org.sonic.entities.RefreshToken
import org.sonic.helpers.*
import org.sonic.repositories.IRefreshTokenRepository
import org.sonic.repositories.IUserRepository
import java.util.*

class AuthService(
    private val jwtSecret: String,
    private val userRepository: IUserRepository,
    private val refreshTokenRepository: IRefreshTokenRepository,
) {
    suspend fun postRegister(call: ApplicationCall) {
        val request = call.receive<AuthRequest>()
        val validator = ValidatorHelper(request.toMap())
        validator.required("name", "Nama tidak boleh kosong")
        validator.required("username", "Username tidak boleh kosong")
        validator.required("email", "Email tidak boleh kosong")
        validator.required("password", "Password tidak boleh kosong")
        validator.validate()

        if (userRepository.getByUsername(request.username) != null)
            throw AppException(409, "Username sudah digunakan!")
        if (userRepository.getByEmail(request.email) != null)
            throw AppException(409, "Email sudah terdaftar!")

        request.password = hashPassword(request.password)
        val userId = userRepository.create(request.toEntity())
        call.respond(DataResponse("success", "Berhasil mendaftar",
            AuthRegisterData(userId))) // ← FIX: ganti mapOf
    }

    suspend fun postLogin(call: ApplicationCall) {
        val request = call.receive<AuthRequest>()
        val validator = ValidatorHelper(request.toMap())
        validator.required("email", "Email tidak boleh kosong")
        validator.required("password", "Password tidak boleh kosong")
        validator.validate()

        val existUser = userRepository.getByEmail(request.email)
            ?: throw AppException(401, "Kredensial tidak valid!")
        if (!verifyPassword(request.password, existUser.password))
            throw AppException(401, "Kredensial tidak valid!")

        refreshTokenRepository.deleteByUserId(existUser.id)
        val authToken = generateAuthToken(existUser.id, jwtSecret)
        val strRefreshToken = UUID.randomUUID().toString()
        refreshTokenRepository.create(
            RefreshToken(userId = existUser.id, authToken = authToken, refreshToken = strRefreshToken)
        )

        call.respond(DataResponse("success", "Berhasil login",
            AuthTokenData(authToken, strRefreshToken))) // ← FIX: ganti mapOf
    }

    suspend fun postRefreshToken(call: ApplicationCall) {
        val request = call.receive<RefreshTokenRequest>()
        val validator = ValidatorHelper(request.toMap())
        validator.required("refreshToken", "Refresh token tidak boleh kosong")
        validator.required("authToken", "Auth token tidak boleh kosong")
        validator.validate()

        val existToken = refreshTokenRepository.getByToken(request.refreshToken, request.authToken)
        refreshTokenRepository.delete(request.authToken)
        if (existToken == null) throw AppException(401, "Token tidak valid!")

        val user = userRepository.getById(existToken.userId)
            ?: throw AppException(404, "User tidak valid!")
        val authToken = generateAuthToken(user.id, jwtSecret)
        val strRefreshToken = UUID.randomUUID().toString()
        refreshTokenRepository.create(
            RefreshToken(userId = user.id, authToken = authToken, refreshToken = strRefreshToken)
        )

        call.respond(DataResponse("success", "Token berhasil diperbarui",
            AuthTokenData(authToken, strRefreshToken))) // ← FIX: ganti mapOf
    }

    suspend fun postLogout(call: ApplicationCall) {
        val request = call.receive<RefreshTokenRequest>()
        val validator = ValidatorHelper(request.toMap())
        validator.required("authToken", "Auth token tidak boleh kosong")
        validator.validate()

        val decoded = JWT.require(Algorithm.HMAC256(jwtSecret)).build().verify(request.authToken)
        val userId = decoded.getClaim("userId").asString()
            ?: throw AppException(401, "Token tidak valid")
        refreshTokenRepository.delete(request.authToken)
        refreshTokenRepository.deleteByUserId(userId)
        call.respond(DataResponse("success", "Berhasil logout", null))
    }

    private fun generateAuthToken(userId: String, secret: String): String {
        return JWT.create()
            .withAudience(JWTConstants.AUDIENCE)
            .withIssuer(JWTConstants.ISSUER)
            .withClaim("userId", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
            .sign(Algorithm.HMAC256(secret))
    }
}