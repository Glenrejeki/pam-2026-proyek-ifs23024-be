package org.sonic.entities

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Follow(
    var id: String = UUID.randomUUID().toString(),
    var followerId: String,
    var followingId: String,
    @Contextual val createdAt: Instant = Clock.System.now(),
)