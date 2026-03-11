package org.sonic.dao

import org.sonic.tables.TweetTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import java.util.UUID

class TweetDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, TweetDAO>(TweetTable)
    var userId by TweetTable.userId
    var content by TweetTable.content
    var imageUrl by TweetTable.imageUrl
    var retweetOfId by TweetTable.retweetOfId
    var quoteOfId by TweetTable.quoteOfId
    var replyToId by TweetTable.replyToId
    var language by TweetTable.language
    var likesCount by TweetTable.likesCount
    var retweetsCount by TweetTable.retweetsCount
    var repliesCount by TweetTable.repliesCount
    var quotesCount by TweetTable.quotesCount
    var createdAt by TweetTable.createdAt
    var updatedAt by TweetTable.updatedAt
}