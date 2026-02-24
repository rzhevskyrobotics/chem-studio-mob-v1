package com.example.smart_labs.auth

import com.example.smart_labs.network.ApiClient
import com.example.smart_labs.network.LoginRequest

class AuthRepositoryImpl : AuthRepository {

    override suspend fun login(login: String, password: String): Result<String> {
        return try {
            val response = ApiClient.api.login(
                LoginRequest(login = login, password = password)
            )

            if (!response.isSuccessful) {
                val msg = when (response.code()) {
                    401 -> "Неправильный логин или пароль"
                    else -> "Сервер: ${response.code()} ${response.message()}"
                }
                return Result.failure(IllegalStateException(msg))
            }

            val body = response.body()
            val status = body?.status
            val data = body?.data

            if (status?.code == 0 && data != null) {
                Result.success(data.token)
            } else {
                Result.failure(IllegalStateException(status?.message ?: "Ошибка авторизации"))
            }
        } catch (e: Exception) {
            Result.failure(IllegalStateException("Ошибка сети: ${e.localizedMessage ?: "unknown"}", e))
        }
    }
}