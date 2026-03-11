package org.sonic.dao

import org.sonic.tables.UserTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import java.util.UUID

class UserDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, UserDAO>(UserTable)
    var name by UserTable.name
    var username by UserTable.username
    var email by UserTable.email
    var password by UserTable.password
    var bio by UserTable.bio
    var photo by UserTable.photo
    var headerPhoto by UserTable.headerPhoto
    var location by UserTable.location
    var website by UserTable.website
    var isVerified by UserTable.isVerified
    var followersCount by UserTable.followersCount
    var followingCount by UserTable.followingCount
    var createdAt by UserTable.createdAt
    var updatedAt by UserTable.updatedAt
}