package com.example.smart_labs

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.content.edit
import com.example.smart_labs.auth.AuthRepositoryImpl
import com.example.smart_labs.login.LoginUiState
import com.example.smart_labs.login.LoginViewModel
import com.example.smart_labs.ui.theme.Smart_LabsTheme
import com.example.smart_labs.ui.components.AppHeader

class LoginActivity : ComponentActivity() {

    private val viewModel: LoginViewModel by viewModels {
        LoginViewModel.Factory(AuthRepositoryImpl())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(AuthPrefs.PREFS_NAME, MODE_PRIVATE)
        if (prefs.getBoolean(AuthPrefs.KEY_IS_LOGGED_IN, false)) {
            goToMain()
            return
        }

        enableEdgeToEdge()
        setContent {
            Smart_LabsTheme {
                LoginScreen(
                    vm = viewModel,
                    onLoginSuccess = { login, token ->
                        prefs.edit()
                            .putBoolean(AuthPrefs.KEY_IS_LOGGED_IN, true)
                            .putString(AuthPrefs.KEY_LOGIN, login)
                            .putString(AuthPrefs.KEY_TOKEN, token)
                            .apply()
                        goToMain()
                    }
                )
            }
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

@Composable
fun LoginScreen(
    vm: LoginViewModel,
    onLoginSuccess: (String, String) -> Unit // login, token
) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val state by vm.uiState.collectAsStateWithLifecycle()

    // one-time navigation on success
    LaunchedEffect(state) {
        if (state is LoginUiState.Success) {
            val token = (state as LoginUiState.Success).token
            onLoginSuccess(login, token)
            vm.resetToIdle()
        }
    }

    Surface(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            AppHeader(
                showScreenTitle = true,
                screenTitle = "Авторизация"
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = login,
                onValueChange = {
                    login = it
                    if (state is LoginUiState.Error) vm.resetToIdle()
                },
                label = { Text("Логин") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (state is LoginUiState.Error) vm.resetToIdle()
                },
                label = { Text("Пароль") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            if (state is LoginUiState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = (state as LoginUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val loading = state is LoginUiState.Loading

            Button(
                onClick = { vm.submit(login, password) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading
            ) {
                Text(if (loading) "Вход..." else "Войти")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    Smart_LabsTheme {
        LoginScreen(
            vm = LoginViewModel(AuthRepositoryImpl()),
            onLoginSuccess = { _, _ -> }
        )
    }
}