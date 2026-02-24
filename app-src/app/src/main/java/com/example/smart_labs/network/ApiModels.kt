package com.example.smart_labs.network

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val login: String,
    val password: String
)

data class StatusResponse(
    val code: Int,
    val message: String
)

data class LoginData(
    val token: String,
    @SerializedName("expires_at")
    val expiresAt: Long
)

data class LoginApiResponse(
    val status: StatusResponse,
    val data: LoginData?
)

// ------- MQTT config --------
data class MqttConfigData(
    val broker: String,
    val username: String,
    val password: String,
)

data class MqttConfigResponse(
    val status: StatusResponse,
    val data: MqttConfigData?
)