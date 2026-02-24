package com.example.smart_labs.mqtt

fun parseFloatOrNull(payload: String): Float? =
    payload.trim().replace(",", ".").toFloatOrNull()

fun parseBoolOrNull(payload: String): Boolean? {
    return when (payload.trim().lowercase()) {
        "true", "1", "on", "yes" -> true
        "false", "0", "off", "no" -> false
        else -> null
    }
}
