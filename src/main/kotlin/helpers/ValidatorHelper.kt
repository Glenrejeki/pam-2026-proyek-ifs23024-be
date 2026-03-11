package org.sonic.helpers

import org.sonic.data.AppException

class ValidatorHelper(private val data: Map<String, Any?>) {
    private val errors = mutableListOf<String>()

    fun required(field: String, message: String? = null) {
        val value = data[field]
        if (value == null || (value is String && value.isBlank()))
            errors.add("$field:${message ?: "$field is required"}")
    }

    fun minLength(field: String, min: Int, message: String? = null) {
        val value = data[field]
        if (value is String && value.length < min)
            errors.add("$field:${message ?: "$field must be at least $min characters"}")
    }

    fun validate() {
        if (errors.isNotEmpty()) throw AppException(400, errors.joinToString("|"))
    }
}