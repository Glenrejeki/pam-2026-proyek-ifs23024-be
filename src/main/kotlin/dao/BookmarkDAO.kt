package org.sonic.dao

import org.sonic.tables.BookmarkTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import java.util.UUID

class BookmarkDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, BookmarkDAO>(BookmarkTable)
    var userId by BookmarkTable.userId
    var tweetId by BookmarkTable.tweetId
    var createdAt by BookmarkTable.createdAt
}