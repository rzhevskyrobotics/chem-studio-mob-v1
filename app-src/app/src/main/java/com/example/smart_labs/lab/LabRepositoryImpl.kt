package com.example.smart_labs.lab

import com.example.smart_labs.network.ApiClient
import com.example.smart_labs.network.MqttConfigResponse

class LabRepositoryImpl : LabRepository {

    override suspend fun loadMqttConfig(token: String): Result<MqttConfigResponse> {
        return try {
            val resp = ApiClient.api.getMqttConfig("Bearer $token")

            if (!resp.isSuccessful) {
                return Result.failure(
                    IllegalStateException("Не удалось получить MQTT конфиг: ${resp.code()}")
                )
            }

            val body = resp.body()
                ?: return Result.failure(IllegalStateException("Пустой ответ от сервера"))

            val cfg = body.data
            if (body.status.code != 0 || cfg == null) {
                return Result.failure(
                    IllegalStateException(body.status.message ?: "Ошибка MQTT конфига")
                )
            }

            Result.success(body)
        } catch (e: Exception) {
            Result.failure(IllegalStateException("Ошибка сети: ${e.localizedMessage ?: "unknown"}", e))
        }
    }
}