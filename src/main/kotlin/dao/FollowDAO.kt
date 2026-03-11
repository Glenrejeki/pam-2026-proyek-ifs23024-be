package org.sonic.dao

import org.sonic.tables.FollowTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import java.util.UUID

class FollowDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, FollowDAO>(FollowTable)
    var followerId by FollowTable.followerId
    var followingId by FollowTable.followingId
    var createdAt by FollowTable.createdAt
}