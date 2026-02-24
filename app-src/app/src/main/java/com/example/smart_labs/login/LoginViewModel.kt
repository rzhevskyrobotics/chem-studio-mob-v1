package com.example.smart_labs.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_labs.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun submit(login: String, password: String) {
        if (login.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Введите логин и пароль")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val res = repo.login(login.trim(), password)
            _uiState.value = res.fold(
                onSuccess = { token -> LoginUiState.Success(token) },
                onFailure = { e -> LoginUiState.Error(e.message ?: "Ошибка авторизации") }
            )
        }
    }

    fun resetToIdle() {
        _uiState.value = LoginUiState.Idle
    }

    class Factory(
        private val repo: AuthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                return LoginViewModel(repo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
        }
    }
}