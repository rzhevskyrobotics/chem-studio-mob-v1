package com.example.smart_labs.main
import com.example.smart_labs.catalog.TopicCatalogScreen

import com.example.smart_labs.ui.components.AppHeader
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smart_labs.ui.theme.Smart_LabsTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription


@Composable
fun MainScreen(
    vm: MainViewModel,
    onLogout: () -> Unit
) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.start()
        vm.events.collect { e ->
            when (e) {
                is MainViewModel.UiEvent.Snackbar -> snackbar.showSnackbar(e.message)
            }
        }
    }

    var showCatalog by remember { mutableStateOf(false) }

    if (showCatalog) {
        TopicCatalogScreen(onBack = { showCatalog = false })
        return
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {

                AppHeader(showScreenTitle = false)

                Spacer(modifier = Modifier.height(16.dp))

                // Кнопка выхода
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(onClick = {
                        vm.disconnectNow()
                        onLogout()
                    }) {
                        Text("Выйти из сессии")
                    }
                    Spacer(Modifier.width(12.dp))

                    OutlinedButton(onClick = { showCatalog = true }) {
                        Text("Справочник")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Статус MQTT/загрузка
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Загрузка…")
                    } else {
                        Text(
                            text = if (state.isMqttConnected) "MQTT подключён" else "MQTT не подключён",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Умная лаборатория",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Температура на улице
                OutdoorTemperatureCard(currentTemp = state.outTemp)
                Spacer(modifier = Modifier.height(12.dp))

                // Температура в лаборатории
                LabTemperatureCard(currentTemp = state.labTemp)
                Spacer(modifier = Modifier.height(12.dp))

                // Температура в реакторе (target и контроль)
                ReactorTemperatureCard(
                    currentTemp = state.reactTemp,
                    targetTemp = state.reactTargetTemp,
                    controlEnabled = state.reactControlEnabled,
                    controlLoading = state.loadingReactControl,
                    onTargetTempChange = { vm.onReactTargetTempChanged(it) },
                    onControlToggle = { newValue ->
                        vm.onReactControlToggle(newValue)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Давление в реакторе
                ReactorPressureCard(pressure = state.reactPress)

                Spacer(modifier = Modifier.height(12.dp))

                // Дозаторы
                DispenserCard(
                    title = "Дозатор 1",
                    speed = state.disp1Speed,
                    enabled = state.disp1Enabled,
                    toggleLoading = state.loadingDisp1Pow,
                    onSpeedChange = { vm.onDisp1SpeedChanged(it) },
                    onToggle = { newValue ->
                        vm.onDisp1Toggle(newValue)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                DispenserCard(
                    title = "Дозатор 2",
                    speed = state.disp2Speed,
                    enabled = state.disp2Enabled,
                    toggleLoading = state.loadingDisp2Pow,
                    onSpeedChange = { vm.onDisp2SpeedChanged(it) },
                    onToggle = { newValue ->
                        vm.onDisp1Toggle(newValue)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            SnackbarHost(
                hostState = snackbar,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun OutdoorTemperatureCard(currentTemp: Float?) {
    val tempText = currentTemp?.let { String.format("%.1f °C", it) } ?: "—"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Температура на улице: $tempText" }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Температура на улице", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = tempText, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
fun LabTemperatureCard(currentTemp: Float?) {
    val tempText = currentTemp?.let { String.format("%.1f °C", it) } ?: "—"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Температура в лаборатории: $tempText" }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Температура в лаборатории", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = tempText, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
fun ReactorTemperatureCard(
    currentTemp: Float?,
    targetTemp: Float,
    controlEnabled: Boolean,
    controlLoading: Boolean,
    onTargetTempChange: (Float) -> Unit,
    onControlToggle: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = "Температура в реакторе",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            val currText = currentTemp?.let { String.format("%.1f °C", it) } ?: "—"
            Text(
                text = "Текущая: $currText",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Заданная: " + String.format("%.1f °C", targetTemp),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                modifier = Modifier.semantics {
                    contentDescription = "Заданная температура: ${String.format("%.1f", targetTemp)} градусов"
                },
                value = targetTemp,
                onValueChange = { onTargetTempChange(it) },
                valueRange = 20f..120f,
                enabled = !controlLoading
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {

                Text(
                    text = "Контроль температуры",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                if (controlLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Switch(
                    modifier = Modifier.semantics {
                        stateDescription = if (controlEnabled) "Включено" else "Выключено"
                    },
                    checked = controlEnabled,
                    onCheckedChange = { newValue ->
                        if (!controlLoading) onControlToggle(newValue)
                    },
                    enabled = !controlLoading
                )
            }
        }
    }
}

@Composable
fun ReactorPressureCard(pressure: Float?) {
    val pText = pressure?.let { String.format("%.2f бар", it) } ?: "—"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Давление в реакторе: $pText" }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Давление в реакторе", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = pText, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
fun DispenserCard(
    title: String,
    speed: Float,
    enabled: Boolean,
    toggleLoading: Boolean,
    onSpeedChange: (Float) -> Unit,
    onToggle: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {

                Text(
                    text = "Скорость: ${speed.toInt()} мл/мин",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                if (toggleLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Switch(
                    modifier = Modifier.semantics {
                        stateDescription = if (enabled) "Включено" else "Выключено"
                    },
                    checked = enabled,
                    onCheckedChange = { newValue ->
                        if (!toggleLoading) onToggle(newValue)
                    },
                    enabled = !toggleLoading
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                modifier = Modifier.semantics {
                    contentDescription = "Скорость: ${speed.toInt()} миллилитров в минуту"
                },
                value = speed.coerceIn(1f, 50f),
                onValueChange = { onSpeedChange(it.coerceIn(1f, 50f)) },
                valueRange = 1f..50f,
                enabled = !toggleLoading
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    Smart_LabsTheme {
        // Preview без реального VM — просто показываем UI.
        Text("Preview not available for MainScreen without ViewModel")
    }
}