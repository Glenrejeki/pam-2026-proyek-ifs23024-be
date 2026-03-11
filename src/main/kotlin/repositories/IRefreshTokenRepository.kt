package org.sonic.repositories

import org.sonic.entities.RefreshToken

interface IRefreshTokenRepository {
    suspend fun getByToken(refreshToken: String, authToken: String): RefreshToken?
    suspend fun create(newRefreshToken: RefreshToken): String
    suspend fun delete(authToken: String): Boolean
    suspend fun deleteByUserId(userId: String): Boolean
}