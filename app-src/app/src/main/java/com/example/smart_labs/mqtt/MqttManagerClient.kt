package com.example.smart_labs.mqtt

import kotlinx.coroutines.channels.awaitClose

class MqttManagerClient : MqttClient {

    override fun connectAndSubscribe(
        serverUri: String,
        username: String?,
        password: String?,
        topics: List<String>
    ): kotlinx.coroutines.flow.Flow<MqttEvent> {
        return kotlinx.coroutines.flow.callbackFlow {
            // Развешиваем handlers на каждый topic
            topics.forEach { topic ->
                MqttManager.setHandler(topic) { payload ->
                    trySend(MqttEvent.Message(topic, payload))
                }
            }

            // Подключение
            MqttManager.connectAndSubscribe(
                broker = serverUri,
                username = username ?: "",
                password = password ?: "",
                topics = topics,
                onConnected = {
                    trySend(MqttEvent.Connected)
                },
                onError = { msg ->
                    trySend(MqttEvent.Error(msg))
                }
            )

            awaitClose {
                MqttManager.disconnect()
                trySend(MqttEvent.Disconnected("closed"))
            }
        }
    }

    override suspend fun publish(topic: String, payload: String) {
        // fire-and-forget (как у тебя и было)
        MqttManager.publish(
            topic = topic,
            payload = payload,
            retained = true,
            qos = 1
        )
    }

    override fun disconnect() {
        MqttManager.disconnect()
    }
}