package org.sonic.data

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    var id: String = "",
    var name: String = "",
    var username: String = "",
    var email: String = "",
    var bio: String? = null,
    var urlPhoto: String = "",
    var urlHeaderPhoto: String = "",
    var location: String? = null,
    var website: String? = null,
    var isVerified: Boolean = false,
    var followersCount: Int = 0,
    var followingCount: Int = 0,
    var isFollowing: Boolean = false,
    @Contextual var createdAt: Instant = Clock.System.now(),
    @Contextual var updatedAt: Instant = Clock.System.now(),
)