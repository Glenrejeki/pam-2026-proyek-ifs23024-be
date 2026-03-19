package org.sonic.services

import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import org.sonic.data.*
import org.sonic.helpers.*
import org.sonic.repositories.IFollowRepository
import org.sonic.repositories.IRefreshTokenRepository
import org.sonic.repositories.IUserRepository
import java.io.File
import java.util.UUID

class UserService(
    private val userRepo: IUserRepository,
    private val followRepo: IFollowRepository,
    private val refreshTokenRepo: IRefreshTokenRepository,
) {
    suspend fun getMe(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)
        call.respond(DataResponse("success", "Data profil berhasil diambil",
            UserData(UserResponse(
                id = user.id, name = user.name, username = user.username, email = user.email,
                bio = user.bio, urlPhoto = user.urlPhoto, urlHeaderPhoto = user.urlHeaderPhoto,
                location = user.location, website = user.website, isVerified = user.isVerified,
                followersCount = user.followersCount, followingCount = user.followingCount,
                createdAt = user.createdAt, updatedAt = user.updatedAt
            ))
        ))
    }

    suspend fun getUserProfile(call: ApplicationCall) {
        val username = call.parameters["username"] ?: throw AppException(400, "Username tidak valid!")
        val authUserId = try { ServiceHelper.getAuthUserId(call) } catch (e: Exception) { null }
        val user = userRepo.getByUsername(username) ?: throw AppException(404, "Pengguna tidak ditemukan!")
        val isFollowing = authUserId?.let { followRepo.isFollowing(it, user.id) } ?: false

        call.respond(DataResponse("success", "Profil berhasil diambil",
            UserData(UserResponse(
                id = user.id, name = user.name, username = user.username, email = user.email,
                bio = user.bio, urlPhoto = user.urlPhoto, urlHeaderPhoto = user.urlHeaderPhoto,
                location = user.location, website = user.website, isVerified = user.isVerified,
                followersCount = user.followersCount, followingCount = user.followingCount,
                isFollowing = isFollowing, createdAt = user.createdAt, updatedAt = user.updatedAt
            ))
        ))
    }

    suspend fun putMe(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)
        val request = call.receive<AuthRequest>()
        val validator = ValidatorHelper(request.toMap())
        validator.required("name", "Nama tidak boleh kosong")
        validator.required("username", "Username tidak boleh kosong")
        validator.validate()

        val existUser = userRepo.getByUsername(request.username)
        if (existUser != null && existUser.id != user.id)
            throw AppException(409, "Username sudah digunakan!")

        user.name = request.name
        user.username = request.username
        if (!userRepo.update(user.id, user)) throw AppException(400, "Gagal memperbarui profil!")
        call.respond(DataResponse("success", "Profil berhasil diperbarui", null))
    }

    suspend fun putMyProfile(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)
        val request = call.receive<Map<String, String>>()
        request["bio"]?.let { user.bio = it.ifBlank { null } }
        request["location"]?.let { user.location = it.ifBlank { null } }
        request["website"]?.let { user.website = it.ifBlank { null } }
        request["name"]?.let { if (it.isNotBlank()) user.name = it }
        userRepo.update(user.id, user)
        call.respond(DataResponse("success", "Profil berhasil diperbarui", null))
    }

    suspend fun putMyPhoto(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)
        var newPhoto: String? = null

        call.receiveMultipart(formFieldLimit = 1024 * 1024 * 5).forEachPart { part ->
            if (part is PartData.FileItem) {
                val ext = part.originalFileName?.substringAfterLast('.', "")
                    ?.let { if (it.isNotEmpty()) ".$it" else "" } ?: ""
                val filePath = "uploads/users/${UUID.randomUUID()}$ext"
                File(filePath).also { it.parentFile.mkdirs() }.let { file ->
                    part.provider().copyAndClose(file.writeChannel())
                }
                newPhoto = filePath
            }
            part.dispose()
        }

        val path = newPhoto ?: throw AppException(400, "Foto tidak tersedia!")
        if (!File(path).exists()) throw AppException(400, "Foto gagal diunggah!")

        val oldPhoto = user.photo
        user.photo = path
        if (!userRepo.update(user.id, user)) throw AppException(400, "Gagal memperbarui foto!")
        oldPhoto?.let { File(it).takeIf { f -> f.exists() }?.delete() }
        call.respond(DataResponse("success", "Foto profil berhasil diperbarui", null))
    }

    suspend fun putMyHeaderPhoto(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)
        var newPhoto: String? = null

        call.receiveMultipart(formFieldLimit = 1024 * 1024 * 10).forEachPart { part ->
            if (part is PartData.FileItem) {
                val ext = part.originalFileName?.substringAfterLast('.', "")
                    ?.let { if (it.isNotEmpty()) ".$it" else "" } ?: ""
                val filePath = "uploads/users/header_${UUID.randomUUID()}$ext"
                File(filePath).also { it.parentFile.mkdirs() }.let { file ->
                    part.provider().copyAndClose(file.writeChannel())
                }
                newPhoto = filePath
            }
            part.dispose()
        }

        val path = newPhoto ?: throw AppException(400, "Header foto tidak tersedia!")
        val oldHeader = user.headerPhoto
        user.headerPhoto = path
        userRepo.update(user.id, user)
        oldHeader?.let { File(it).takeIf { f -> f.exists() }?.delete() }
        call.respond(DataResponse("success", "Header foto berhasil diperbarui", null))
    }

    suspend fun putMyPassword(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)
        val request = call.receive<AuthRequest>()
        val validator = ValidatorHelper(request.toMap())
        validator.required("password", "Kata sandi lama tidak boleh kosong")
        validator.required("newPassword", "Kata sandi baru tidak boleh kosong")
        validator.validate()

        if (!verifyPassword(request.password, user.password))
            throw AppException(401, "Kata sandi lama tidak valid!")

        user.password = hashPassword(request.newPassword)
        if (!userRepo.update(user.id, user)) throw AppException(400, "Gagal mengubah kata sandi!")
        refreshTokenRepo.deleteByUserId(user.id)
        call.respond(DataResponse("success", "Kata sandi berhasil diubah", null))
    }

    suspend fun follow(call: ApplicationCall) {
        val targetUserId = call.parameters["userId"] ?: throw AppException(400, "User ID tidak valid!")
        val user = ServiceHelper.getAuthUser(call, userRepo)
        if (user.id == targetUserId) throw AppException(400, "Tidak bisa mengikuti diri sendiri!")
        userRepo.getById(targetUserId) ?: throw AppException(404, "Pengguna tidak ditemukan!")
        if (followRepo.isFollowing(user.id, targetUserId)) throw AppException(409, "Sudah mengikuti!")

        followRepo.follow(user.id, targetUserId)
        userRepo.incrementFollowing(user.id)
        userRepo.incrementFollowers(targetUserId)
        call.respond(DataResponse("success", "Berhasil mengikuti pengguna", null))
    }

    suspend fun unfollow(call: ApplicationCall) {
        val targetUserId = call.parameters["userId"] ?: throw AppException(400, "User ID tidak valid!")
        val user = ServiceHelper.getAuthUser(call, userRepo)
        if (!followRepo.isFollowing(user.id, targetUserId)) throw AppException(400, "Belum mengikuti!")

        followRepo.unfollow(user.id, targetUserId)
        userRepo.decrementFollowing(user.id)
        userRepo.decrementFollowers(targetUserId)
        call.respond(DataResponse("success", "Berhenti mengikuti", null))
    }

    suspend fun getFollowers(call: ApplicationCall) {
        val userId = call.parameters["userId"] ?: throw AppException(400, "User ID tidak valid!")
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val limit = (call.request.queryParameters["limit"]?.toIntOrNull() ?: 20).coerceAtMost(50)
        val authUserId = try { ServiceHelper.getAuthUserId(call) } catch (e: Exception) { null }

        val followers = followRepo.getFollowers(userId, page, limit)
            .mapNotNull { userRepo.getById(it) }
            .map { u -> UserResponse(
                id = u.id, name = u.name, username = u.username, email = u.email,
                urlPhoto = u.urlPhoto, isVerified = u.isVerified,
                followersCount = u.followersCount, followingCount = u.followingCount,
                isFollowing = authUserId?.let { followRepo.isFollowing(it, u.id) } ?: false,
                createdAt = u.createdAt, updatedAt = u.updatedAt
            )}
        call.respond(DataResponse("success", "Daftar pengikut berhasil diambil",
            UsersData(followers, page, limit)))
    }

    suspend fun getFollowing(call: ApplicationCall) {
        val userId = call.parameters["userId"] ?: throw AppException(400, "User ID tidak valid!")
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val limit = (call.request.queryParameters["limit"]?.toIntOrNull() ?: 20).coerceAtMost(50)
        val authUserId = try { ServiceHelper.getAuthUserId(call) } catch (e: Exception) { null }

        val following = followRepo.getFollowing(userId, page, limit)
            .mapNotNull { userRepo.getById(it) }
            .map { u -> UserResponse(
                id = u.id, name = u.name, username = u.username, email = u.email,
                urlPhoto = u.urlPhoto, isVerified = u.isVerified,
                followersCount = u.followersCount, followingCount = u.followingCount,
                isFollowing = authUserId?.let { followRepo.isFollowing(it, u.id) } ?: false,
                createdAt = u.createdAt, updatedAt = u.updatedAt
            )}
        call.respond(DataResponse("success", "Daftar yang diikuti berhasil diambil",
            UsersData(following, page, limit)))
    }

    suspend fun searchUsers(call: ApplicationCall) {
        val query = call.request.queryParameters["q"] ?: throw AppException(400, "Query tidak boleh kosong!")
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val limit = (call.request.queryParameters["limit"]?.toIntOrNull() ?: 20).coerceAtMost(50)
        val authUserId = try { ServiceHelper.getAuthUserId(call) } catch (e: Exception) { null }

        val users = userRepo.search(query, page, limit).map { u ->
            UserResponse(
                id = u.id, name = u.name, username = u.username, email = u.email,
                bio = u.bio, urlPhoto = u.urlPhoto, isVerified = u.isVerified,
                followersCount = u.followersCount, followingCount = u.followingCount,
                isFollowing = authUserId?.let { followRepo.isFollowing(it, u.id) } ?: false,
                createdAt = u.createdAt, updatedAt = u.updatedAt
            )
        }
        call.respond(DataResponse("success", "Hasil pencarian pengguna",
            SearchUsersData(users, query, page, limit)))
    }

    suspend fun getPhoto(call: ApplicationCall) {
        val userId = call.parameters["id"] ?: throw AppException(400, "User ID tidak valid!")
        val user = userRepo.getById(userId) ?: throw AppException(404, "Pengguna tidak ditemukan!")
        val path = user.photo ?: throw AppException(404, "Belum memiliki foto profil!")
        val file = File(path)
        if (!file.exists()) throw AppException(404, "Foto tidak tersedia!")
        call.respondFile(file)
    }
}