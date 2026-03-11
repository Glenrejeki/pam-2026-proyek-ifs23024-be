package org.sonic.helpers

import org.mindrot.jbcrypt.BCrypt

fun parseMessageToMap(rawMessage: String): Map<String, List<String>> {
    return rawMessage.split("|").mapNotNull { part ->
        val split = part.split(":", limit = 2)
        if (split.size == 2) split[0].trim() to listOf(split[1].trim()) else null
    }.toMap()
}

fun hashPassword(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt(12))
fun verifyPassword(password: String, hashed: String): Boolean = BCrypt.checkpw(password, hashed)

fun extractHashtags(content: String): List<String> {
    return Regex("#(\\w+)").findAll(content).map { it.groupValues[1].lowercase() }.toList()
}