package com.example.smart_labs.lab

import com.example.smart_labs.network.MqttConfigResponse

interface LabRepository {
    suspend fun loadMqttConfig(token: String): Result<MqttConfigResponse>
}