package org.sonic.dao

import org.sonic.tables.HashtagTable
import org.sonic.tables.TweetHashtagTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import java.util.UUID

class HashtagDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, HashtagDAO>(HashtagTable)
    var tag by HashtagTable.tag
    var count by HashtagTable.count
    var updatedAt by HashtagTable.updatedAt
}

class TweetHashtagDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, TweetHashtagDAO>(TweetHashtagTable)
    var tweetId by TweetHashtagTable.tweetId
    var hashtagId by TweetHashtagTable.hashtagId
}