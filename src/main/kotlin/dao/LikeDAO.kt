package org.sonic.dao

import org.sonic.tables.LikeTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import java.util.UUID

class LikeDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, LikeDAO>(LikeTable)
    var userId by LikeTable.userId
    var tweetId by LikeTable.tweetId
    var createdAt by LikeTable.createdAt
}