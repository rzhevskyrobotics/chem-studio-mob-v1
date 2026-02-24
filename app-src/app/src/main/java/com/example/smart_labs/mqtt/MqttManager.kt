package com.example.smart_labs.mqtt

import android.os.Handler
import android.os.Looper
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.util.concurrent.ConcurrentHashMap

object MqttManager {

    private var client: MqttAsyncClient? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val handlers = ConcurrentHashMap<String, (String) -> Unit>()

    fun setHandler(topic: String, handler: (String) -> Unit) {
        handlers[topic] = handler
    }

    fun connectAndSubscribe(
        broker: String,
        username: String,
        password: String,
        topics: List<String>,
        onConnected: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (client?.isConnected == true) return

        try {
            val clientId = MqttAsyncClient.generateClientId()
            client = MqttAsyncClient(broker, clientId, MemoryPersistence())

            val options = MqttConnectOptions().apply {
                isCleanSession = false
                userName = username
                this.password = password.toCharArray()
                connectionTimeout = 10
                keepAliveInterval = 30
                isAutomaticReconnect = true
            }

            client?.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {}

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    val t = topic ?: return
                    val payload = message?.toString()?.trim() ?: return
                    val h = handlers[t] ?: return
                    mainHandler.post { h(payload) }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
            })

            client?.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    try {
                        val qos = IntArray(topics.size) { 0 }
                        client?.subscribe(topics.toTypedArray(), qos)
                        mainHandler.post {
                            onConnected()
                        }
                    } catch (e: Exception) {
                        mainHandler.post { onError("Ошибка подписки: ${e.localizedMessage}") }
                    }
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    mainHandler.post { onError("Ошибка MQTT подключения: ${exception?.localizedMessage}") }
                }
            })

        } catch (e: Exception) {
            onError("Ошибка MQTT: ${e.localizedMessage}")
        }
    }

    fun publish(
        topic: String,
        payload: String,
        retained: Boolean = true,
        qos: Int = 1,
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        val c = client
        if (c == null || c.isConnected != true) {
            onError?.invoke("MQTT не подключён")
            return
        }

        try {
            val msg = MqttMessage(payload.toByteArray()).apply {
                this.qos = qos
                isRetained = retained
            }
            c.publish(topic, msg, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    onSuccess?.let { mainHandler.post { it() } }
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    onError?.let { mainHandler.post { it("Ошибка публикации: ${exception?.localizedMessage}") } }
                }
            })
        } catch (e: Exception) {
            onError?.invoke("Ошибка публикации: ${e.localizedMessage}")
        }
    }

    fun disconnect() {
        try {
            client?.disconnect()
        } catch (_: Exception) {
        } finally {
            client = null
            handlers.clear()
        }
    }
}
