package org.sonic.repositories

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.sonic.dao.RefreshTokenDAO
import org.sonic.entities.RefreshToken
import org.sonic.helpers.refreshTokenDAOToModel
import org.sonic.helpers.suspendTransaction
import org.sonic.tables.RefreshTokenTable
import java.util.UUID

class RefreshTokenRepository : IRefreshTokenRepository {

    override suspend fun getByToken(refreshToken: String, authToken: String): RefreshToken? = suspendTransaction {
        RefreshTokenDAO.find {
            (RefreshTokenTable.refreshToken eq refreshToken) and (RefreshTokenTable.authToken eq authToken)
        }.limit(1).map(::refreshTokenDAOToModel).firstOrNull()
    }

    override suspend fun create(newRefreshToken: RefreshToken): String = suspendTransaction {
        RefreshTokenDAO.new {
            userId = UUID.fromString(newRefreshToken.userId)
            refreshToken = newRefreshToken.refreshToken
            authToken = newRefreshToken.authToken
            createdAt = newRefreshToken.createdAt
        }.id.value.toString()
    }

    override suspend fun delete(authToken: String): Boolean = suspendTransaction {
        RefreshTokenTable.deleteWhere { RefreshTokenTable.authToken eq authToken } >= 1
    }

    override suspend fun deleteByUserId(userId: String): Boolean = suspendTransaction {
        RefreshTokenTable.deleteWhere { RefreshTokenTable.userId eq UUID.fromString(userId) } >= 1
    }
}