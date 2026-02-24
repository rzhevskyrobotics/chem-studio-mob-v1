package com.example.smart_labs

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.smart_labs.lab.LabRepositoryImpl
import com.example.smart_labs.main.MainScreen
import com.example.smart_labs.main.MainViewModel
import com.example.smart_labs.mqtt.MqttManagerClient
import com.example.smart_labs.ui.theme.Smart_LabsTheme

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels {
        val prefs = getSharedPreferences(AuthPrefs.PREFS_NAME, MODE_PRIVATE)
        val token = prefs.getString(AuthPrefs.KEY_TOKEN, null).orEmpty()

        MainViewModel.Factory(
            token = token,
            labRepo = LabRepositoryImpl(),
            mqtt = MqttManagerClient()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // защита: если токена нет — на логин
        val prefs = getSharedPreferences(AuthPrefs.PREFS_NAME, MODE_PRIVATE)
        val token = prefs.getString(AuthPrefs.KEY_TOKEN, null)
        if (token.isNullOrBlank()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContent {
            Smart_LabsTheme {
                MainScreen(
                    vm = vm,
                    onLogout = { logout() }
                )
            }
        }
    }

    private fun logout() {
        // на всякий случай отключаем mqtt перед выходом
        vm.disconnectNow()

        val prefs = getSharedPreferences(AuthPrefs.PREFS_NAME, MODE_PRIVATE)
        prefs.edit().clear().apply()

        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}