package com.example.smart_labs.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_labs.lab.LabRepository
import com.example.smart_labs.mqtt.MqttClient
import com.example.smart_labs.mqtt.MqttEvent
import com.example.smart_labs.mqtt.MqttTopics
import com.example.smart_labs.mqtt.parseBoolOrNull
import com.example.smart_labs.mqtt.parseFloatOrNull
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class MainViewModel(
    private val token: String,
    private val labRepo: LabRepository,
    private val mqtt: MqttClient
) : ViewModel() {

    private val _state = kotlinx.coroutines.flow.MutableStateFlow(MainUiState())
    val state: kotlinx.coroutines.flow.StateFlow<MainUiState> = _state

    private var started = false

    sealed interface UiEvent {
        data class Snackbar(val message: String) : UiEvent
    }

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    private fun emitSnackbar(message: String) {
        _events.tryEmit(UiEvent.Snackbar(message))
    }

    // debounce jobs
    private var jobDisp1: Job? = null
    private var jobDisp2: Job? = null
    private var jobReactorTarget: Job? = null

    fun start() {
        if (started) return
        started = true

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val body = labRepo.loadMqttConfig(token).getOrElse { e ->
                _state.value = _state.value.copy(isLoading = false)
                emitSnackbar(e.message ?: "Ошибка загрузки MQTT конфига")
                return@launch
            }

            val cfg = body.data!!
            val broker = cfg.broker
            val username = cfg.username
            val password = cfg.password

            // Подключаем MQTT через Flow-события
            mqtt.connectAndSubscribe(
                serverUri = broker,
                username = username,
                password = password,
                topics = MqttTopics.ALL_SUBSCRIBE
            ).collect { ev ->
                when (ev) {
                    is MqttEvent.Connected -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isMqttConnected = true,
                            error = null
                        )
                    }

                    is MqttEvent.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isMqttConnected = false
                        )
                        emitSnackbar(ev.message)
                    }

                    is MqttEvent.Disconnected -> {
                        _state.value = _state.value.copy(isMqttConnected = false)
                    }

                    is MqttEvent.Message -> applyIncoming(ev.topic, ev.payload)
                }
            }
        }
    }

    fun disconnectNow() {
        mqtt.disconnect()
    }

    private fun applyIncoming(topic: String, payload: String) {
        val s = _state.value
        when (topic) {
            MqttTopics.OUT_TEMP -> _state.value = s.copy(outTemp = parseFloatOrNull(payload))
            MqttTopics.LAB_TEMP -> _state.value = s.copy(labTemp = parseFloatOrNull(payload))
            MqttTopics.REACT_TEMP -> _state.value = s.copy(reactTemp = parseFloatOrNull(payload))
            MqttTopics.REACT_PRESS -> _state.value = s.copy(reactPress = parseFloatOrNull(payload))

            MqttTopics.REACT_TEMP_SET -> {
                parseBoolOrNull(payload)?.let { _state.value = s.copy(reactControlEnabled = it) }
            }
            MqttTopics.REACT_TEMP_TARGET -> {
                parseFloatOrNull(payload)?.let { _state.value = s.copy(reactTargetTemp = it) }
            }

            MqttTopics.DISP_1_POW -> {
                parseBoolOrNull(payload)?.let { _state.value = s.copy(disp1Enabled = it) }
            }
            MqttTopics.DISP_1_SPEED -> {
                parseFloatOrNull(payload)?.let { _state.value = s.copy(disp1Speed = it) }
            }

            MqttTopics.DISP_2_POW -> {
                parseBoolOrNull(payload)?.let { _state.value = s.copy(disp2Enabled = it) }
            }
            MqttTopics.DISP_2_SPEED -> {
                parseFloatOrNull(payload)?.let { _state.value = s.copy(disp2Speed = it) }
            }
        }
    }

    // UI events (ползунки)
    fun onDisp1SpeedChanged(v: Float) {
        _state.value = _state.value.copy(disp1Speed = v)
        jobDisp1?.cancel()
        jobDisp1 = viewModelScope.launch {
            delay(150)
            mqtt.publish(MqttTopics.DISP_1_SPEED, v.toInt().toString())
        }
    }

    fun onDisp2SpeedChanged(v: Float) {
        _state.value = _state.value.copy(disp2Speed = v)
        jobDisp2?.cancel()
        jobDisp2 = viewModelScope.launch {
            delay(150)
            mqtt.publish(MqttTopics.DISP_2_SPEED, v.toInt().toString())
        }
    }

    fun onReactTargetTempChanged(v: Float) {
        _state.value = _state.value.copy(reactTargetTemp = v)
        jobReactorTarget?.cancel()
        jobReactorTarget = viewModelScope.launch {
            delay(150)
            mqtt.publish(MqttTopics.REACT_TEMP_TARGET, String.format("%.1f", v))
        }
    }

    // UI events (переключатели с лоадингом)
    fun onReactControlToggle(newValue: Boolean) {
        if (_state.value.loadingReactControl) return
        _state.value = _state.value.copy(loadingReactControl = true)

        viewModelScope.launch {
            try {
                mqtt.publish(com.example.smart_labs.mqtt.MqttTopics.REACT_TEMP_SET, newValue.toString())
                _state.value = _state.value.copy(
                    loadingReactControl = false,
                    reactControlEnabled = newValue
                )
                emitSnackbar(
                    if (newValue) "Контроль температуры включён"
                    else "Контроль температуры выключен"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(loadingReactControl = false)
                emitSnackbar("Ошибка: ${e.message ?: "unknown"}")
            }
        }
    }

    fun onDisp1Toggle(newValue: Boolean) {
        if (_state.value.loadingDisp1Pow) return
        _state.value = _state.value.copy(loadingDisp1Pow = true)

        viewModelScope.launch {
            try {
                mqtt.publish(com.example.smart_labs.mqtt.MqttTopics.DISP_1_POW, newValue.toString())
                _state.value = _state.value.copy(
                    loadingDisp1Pow = false,
                    disp1Enabled = newValue
                )
                emitSnackbar("Скорость успешно установлена!")
            } catch (e: Exception) {
                _state.value = _state.value.copy(loadingDisp1Pow = false)
                emitSnackbar("Ошибка: ${e.message ?: "unknown"}")
            }
        }
    }

    fun onDisp2Toggle(newValue: Boolean) {
        if (_state.value.loadingDisp2Pow) return
        _state.value = _state.value.copy(loadingDisp2Pow = true)

        viewModelScope.launch {
            try {
                mqtt.publish(com.example.smart_labs.mqtt.MqttTopics.DISP_2_POW, newValue.toString())
                _state.value = _state.value.copy(
                    loadingDisp2Pow = false,
                    disp2Enabled = newValue
                )
                emitSnackbar("Скорость успешно установлена!")
            } catch (e: Exception) {
                _state.value = _state.value.copy(loadingDisp2Pow = false)
                emitSnackbar("Ошибка: ${e.message ?: "unknown"}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mqtt.disconnect()
    }

    class Factory(
        private val token: String,
        private val labRepo: LabRepository,
        private val mqtt: MqttClient
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(token, labRepo, mqtt) as T
            }
            throw IllegalArgumentException("Unknown ViewModel: $modelClass")
        }
    }
}