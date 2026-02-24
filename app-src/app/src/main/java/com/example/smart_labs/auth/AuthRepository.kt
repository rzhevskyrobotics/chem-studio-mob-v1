package com.example.smart_labs.auth

interface AuthRepository {
    /**
     * @return token if success
     */
    suspend fun login(login: String, password: String): Result<String>
}