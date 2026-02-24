package com.example.smart_labs.mqtt

import kotlinx.coroutines.flow.Flow

interface MqttClient {
    fun connectAndSubscribe(
        serverUri: String,
        username: String?,
        password: String?,
        topics: List<String>
    ): Flow<MqttEvent>

    suspend fun publish(topic: String, payload: String)

    fun disconnect()
}

sealed interface MqttEvent {
    data object Connected : MqttEvent
    data class Disconnected(val reason: String? = null) : MqttEvent
    data class Message(val topic: String, val payload: String) : MqttEvent
    data class Error(val message: String, val throwable: Throwable? = null) : MqttEvent
}