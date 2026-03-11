package org.sonic.entities

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Hashtag(
    var id: String = UUID.randomUUID().toString(),
    var tag: String,
    var count: Int = 0,
    @Contextual var updatedAt: Instant = Clock.System.now(),
)