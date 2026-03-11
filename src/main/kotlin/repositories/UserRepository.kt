package org.sonic.repositories

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.sonic.dao.UserDAO
import org.sonic.entities.User
import org.sonic.helpers.suspendTransaction
import org.sonic.helpers.userDAOToModel
import org.sonic.tables.UserTable
import java.util.UUID

class UserRepository(private val baseUrl: String) : IUserRepository {

    override suspend fun getById(userId: String): User? = suspendTransaction {
        UserDAO.find { UserTable.id eq UUID.fromString(userId) }
            .limit(1).map { userDAOToModel(it, baseUrl) }.firstOrNull()
    }

    override suspend fun getByUsername(username: String): User? = suspendTransaction {
        UserDAO.find { UserTable.username eq username }
            .limit(1).map { userDAOToModel(it, baseUrl) }.firstOrNull()
    }

    override suspend fun getByEmail(email: String): User? = suspendTransaction {
        UserDAO.find { UserTable.email eq email }
            .limit(1).map { userDAOToModel(it, baseUrl) }.firstOrNull()
    }

    override suspend fun search(query: String, page: Int, limit: Int): List<User> = suspendTransaction {
        val keyword = "%${query.lowercase()}%"
        UserDAO.find {
            (UserTable.username.lowerCase() like keyword) or
                    (UserTable.name.lowerCase() like keyword)
        }
            .limit(limit).offset(((page - 1) * limit).toLong())  // ✅ Fix
            .map { userDAOToModel(it, baseUrl) }
    }

    override suspend fun create(user: User): String = suspendTransaction {
        UserDAO.new {
            name = user.name
            username = user.username
            email = user.email
            password = user.password
            bio = user.bio
            photo = user.photo
            headerPhoto = user.headerPhoto
            location = user.location
            website = user.website
            isVerified = user.isVerified
            createdAt = user.createdAt
            updatedAt = user.updatedAt
        }.id.value.toString()
    }

    override suspend fun update(id: String, newUser: User): Boolean = suspendTransaction {
        val dao = UserDAO.find { UserTable.id eq UUID.fromString(id) }
            .limit(1).firstOrNull() ?: return@suspendTransaction false
        dao.name = newUser.name
        dao.username = newUser.username
        dao.email = newUser.email
        dao.password = newUser.password
        dao.bio = newUser.bio
        dao.photo = newUser.photo
        dao.headerPhoto = newUser.headerPhoto
        dao.location = newUser.location
        dao.website = newUser.website
        dao.updatedAt = newUser.updatedAt
        true
    }

    override suspend fun delete(id: String): Boolean = suspendTransaction {
        UserTable.deleteWhere { UserTable.id eq UUID.fromString(id) } >= 1
    }

    override suspend fun incrementFollowers(userId: String): Boolean = suspendTransaction {
        UserTable.update({ UserTable.id eq UUID.fromString(userId) }) {
            with(SqlExpressionBuilder) { it.update(UserTable.followersCount, UserTable.followersCount + 1) }
        } >= 1
    }

    override suspend fun decrementFollowers(userId: String): Boolean = suspendTransaction {
        UserTable.update({ UserTable.id eq UUID.fromString(userId) }) {
            with(SqlExpressionBuilder) { it.update(UserTable.followersCount, UserTable.followersCount - 1) }
        } >= 1
    }

    override suspend fun incrementFollowing(userId: String): Boolean = suspendTransaction {
        UserTable.update({ UserTable.id eq UUID.fromString(userId) }) {
            with(SqlExpressionBuilder) { it.update(UserTable.followingCount, UserTable.followingCount + 1) }
        } >= 1
    }

    override suspend fun decrementFollowing(userId: String): Boolean = suspendTransaction {
        UserTable.update({ UserTable.id eq UUID.fromString(userId) }) {
            with(SqlExpressionBuilder) { it.update(UserTable.followingCount, UserTable.followingCount - 1) }
        } >= 1
    }
}