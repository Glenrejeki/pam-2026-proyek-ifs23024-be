package org.sonic.entities

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Hashtag(
    var id: String = UUID.randomUUID().toString(),
    var tag: String,
    var count: Int = 0,
    var updatedAt: String = "",
)