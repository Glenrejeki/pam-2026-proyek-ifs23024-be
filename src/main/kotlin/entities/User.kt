package org.sonic.entities

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class User(
    var id: String = UUID.randomUUID().toString(),
    var name: String,
    var username: String,
    var email: String,
    var password: String,
    var bio: String? = null,
    var photo: String? = null,
    var urlPhoto: String = "",
    var headerPhoto: String? = null,
    var urlHeaderPhoto: String = "",
    var location: String? = null,
    var website: String? = null,
    var isVerified: Boolean = false,
    var followersCount: Int = 0,
    var followingCount: Int = 0,
    @Contextual val createdAt: Instant = Clock.System.now(),
    @Contextual var updatedAt: Instant = Clock.System.now(),
)