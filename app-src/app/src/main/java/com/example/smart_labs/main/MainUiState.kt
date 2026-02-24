package com.example.smart_labs.main

data class MainUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val isMqttConnected: Boolean = false,

    // Сенсоры
    val outTemp: Float? = null,
    val labTemp: Float? = null,
    val reactTemp: Float? = null,
    val reactPress: Float? = null,

    // Управление
    val reactControlEnabled: Boolean = false,
    val reactTargetTemp: Float = 60f,

    val disp1Enabled: Boolean = false,
    val disp1Speed: Float = 10f,

    val disp2Enabled: Boolean = false,
    val disp2Speed: Float = 15f,

    // Лоадеры кнопок
    val loadingReactControl: Boolean = false,
    val loadingDisp1Pow: Boolean = false,
    val loadingDisp2Pow: Boolean = false
)