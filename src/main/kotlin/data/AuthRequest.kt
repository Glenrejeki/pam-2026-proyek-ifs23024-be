package org.sonic.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.sonic.entities.User

@Serializable
data class AuthRequest(
    var name: String = "",
    var username: String = "",
    var email: String = "",
    var password: String = "",
    var newPassword: String = "",
) {
    fun toMap() = mapOf(
        "name" to name,
        "username" to username,
        "email" to email,
        "password" to password,
        "newPassword" to newPassword
    )

    fun toEntity() = User(
        name = name,
        username = username,
        email = email,
        password = password,
        updatedAt = Clock.System.now()
    )
}