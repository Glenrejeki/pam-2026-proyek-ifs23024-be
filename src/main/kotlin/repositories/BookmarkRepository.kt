package org.sonic.repositories

import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.sonic.dao.BookmarkDAO
import org.sonic.helpers.suspendTransaction
import org.sonic.tables.BookmarkTable
import java.util.UUID

class BookmarkRepository : IBookmarkRepository {

    override suspend fun isBookmarked(userId: String, tweetId: String): Boolean = suspendTransaction {
        BookmarkDAO.find {
            (BookmarkTable.userId eq UUID.fromString(userId)) and
                    (BookmarkTable.tweetId eq UUID.fromString(tweetId))
        }.limit(1).count() > 0
    }

    override suspend fun bookmark(userId: String, tweetId: String): Boolean = suspendTransaction {
        BookmarkDAO.new {
            this.userId = UUID.fromString(userId)
            this.tweetId = UUID.fromString(tweetId)
            this.createdAt = Clock.System.now()
        }
        true
    }

    override suspend fun removeBookmark(userId: String, tweetId: String): Boolean = suspendTransaction {
        BookmarkTable.deleteWhere {
            (BookmarkTable.userId eq UUID.fromString(userId)) and
                    (BookmarkTable.tweetId eq UUID.fromString(tweetId))
        } >= 1
    }

    override suspend fun getBookmarkedTweetIds(userId: String, tweetIds: List<String>): Set<String> = suspendTransaction {
        if (tweetIds.isEmpty()) return@suspendTransaction emptySet()
        val uuids = tweetIds.map { UUID.fromString(it) }
        BookmarkDAO.find {
            (BookmarkTable.userId eq UUID.fromString(userId)) and
                    (BookmarkTable.tweetId inList uuids)
        }.map { it.tweetId.toString() }.toSet()
    }

    override suspend fun getBookmarkedTweetIdsList(userId: String, page: Int, limit: Int): List<String> = suspendTransaction {
        BookmarkDAO.find { BookmarkTable.userId eq UUID.fromString(userId) }
            .orderBy(BookmarkTable.createdAt to SortOrder.DESC)
            .limit(limit).offset(((page - 1) * limit).toLong())  // ✅ Fix
            .map { it.tweetId.toString() }
    }
}