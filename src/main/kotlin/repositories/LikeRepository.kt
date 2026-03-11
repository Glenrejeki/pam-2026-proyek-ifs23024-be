package org.sonic.repositories

import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.sonic.dao.LikeDAO
import org.sonic.helpers.suspendTransaction
import org.sonic.tables.LikeTable
import java.util.UUID

class LikeRepository : ILikeRepository {

    override suspend fun isLiked(userId: String, tweetId: String): Boolean = suspendTransaction {
        LikeDAO.find {
            (LikeTable.userId eq UUID.fromString(userId)) and
                    (LikeTable.tweetId eq UUID.fromString(tweetId))
        }.limit(1).count() > 0
    }

    override suspend fun like(userId: String, tweetId: String): Boolean = suspendTransaction {
        LikeDAO.new {
            this.userId = UUID.fromString(userId)
            this.tweetId = UUID.fromString(tweetId)
            this.createdAt = Clock.System.now()
        }
        true
    }

    override suspend fun unlike(userId: String, tweetId: String): Boolean = suspendTransaction {
        LikeTable.deleteWhere {
            (LikeTable.userId eq UUID.fromString(userId)) and
                    (LikeTable.tweetId eq UUID.fromString(tweetId))
        } >= 1
    }

    override suspend fun getLikedTweetIds(userId: String, tweetIds: List<String>): Set<String> = suspendTransaction {
        if (tweetIds.isEmpty()) return@suspendTransaction emptySet()
        val uuids = tweetIds.map { UUID.fromString(it) }
        LikeDAO.find {
            (LikeTable.userId eq UUID.fromString(userId)) and
                    (LikeTable.tweetId inList uuids)
        }.map { it.tweetId.toString() }.toSet()
    }
}