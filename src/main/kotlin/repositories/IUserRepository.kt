package org.sonic.repositories

import org.sonic.entities.User

interface IUserRepository {
    suspend fun getById(userId: String): User?
    suspend fun getByUsername(username: String): User?
    suspend fun getByEmail(email: String): User?
    suspend fun search(query: String, page: Int, limit: Int): List<User>
    suspend fun create(user: User): String
    suspend fun update(id: String, newUser: User): Boolean
    suspend fun delete(id: String): Boolean
    suspend fun incrementFollowers(userId: String): Boolean
    suspend fun decrementFollowers(userId: String): Boolean
    suspend fun incrementFollowing(userId: String): Boolean
    suspend fun decrementFollowing(userId: String): Boolean
}