package org.sonic.repositories

import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.sonic.dao.FollowDAO
import org.sonic.helpers.suspendTransaction
import org.sonic.tables.FollowTable
import java.util.UUID

class FollowRepository : IFollowRepository {

    override suspend fun isFollowing(followerId: String, followingId: String): Boolean = suspendTransaction {
        FollowDAO.find {
            (FollowTable.followerId eq UUID.fromString(followerId)) and
                    (FollowTable.followingId eq UUID.fromString(followingId))
        }.limit(1).count() > 0
    }

    override suspend fun follow(followerId: String, followingId: String): Boolean = suspendTransaction {
        FollowDAO.new {
            this.followerId = UUID.fromString(followerId)
            this.followingId = UUID.fromString(followingId)
            this.createdAt = Clock.System.now()
        }
        true
    }

    override suspend fun unfollow(followerId: String, followingId: String): Boolean = suspendTransaction {
        FollowTable.deleteWhere {
            (FollowTable.followerId eq UUID.fromString(followerId)) and
                    (FollowTable.followingId eq UUID.fromString(followingId))
        } >= 1
    }

    override suspend fun getFollowingIds(userId: String): List<String> = suspendTransaction {
        FollowDAO.find { FollowTable.followerId eq UUID.fromString(userId) }
            .map { it.followingId.toString() }
    }

    override suspend fun getFollowers(userId: String, page: Int, limit: Int): List<String> = suspendTransaction {
        FollowDAO.find { FollowTable.followingId eq UUID.fromString(userId) }
            .limit(limit).offset(((page - 1) * limit).toLong())  // ✅ Fix
            .map { it.followerId.toString() }
    }

    override suspend fun getFollowing(userId: String, page: Int, limit: Int): List<String> = suspendTransaction {
        FollowDAO.find { FollowTable.followerId eq UUID.fromString(userId) }
            .limit(limit).offset(((page - 1) * limit).toLong())  // ✅ Fix
            .map { it.followingId.toString() }
    }
}