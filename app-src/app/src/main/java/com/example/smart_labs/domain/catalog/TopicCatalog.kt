package com.example.smart_labs.domain.catalog

import com.example.smart_labs.domain.model.TopicItem
import com.example.smart_labs.domain.model.TopicKind
import com.example.smart_labs.mqtt.MqttTopics

object TopicCatalog {

    val items: List<TopicItem> = listOf(
        TopicItem(
            id = "out_temp",
            title = "Температура на улице",
            topic = MqttTopics.OUT_TEMP,
            kind = TopicKind.SENSOR,
            description = "Показание датчика температуры на улице"
        ),
        TopicItem(
            id = "lab_temp",
            title = "Температура в лаборатории",
            topic = MqttTopics.LAB_TEMP,
            kind = TopicKind.SENSOR
        ),
        TopicItem(
            id = "react_temp",
            title = "Температура в реакторе",
            topic = MqttTopics.REACT_TEMP,
            kind = TopicKind.SENSOR
        ),
        TopicItem(
            id = "react_press",
            title = "Давление в реакторе",
            topic = MqttTopics.REACT_PRESS,
            kind = TopicKind.SENSOR
        ),

        TopicItem(
            id = "disp1_pow",
            title = "Дозатор 1 питание",
            topic = MqttTopics.DISP_1_POW,
            kind = TopicKind.CONTROL,
            description = "Вкл/выкл дозатор 1"
        ),
        TopicItem(
            id = "disp1_speed",
            title = "Дозатор 1 скорость",
            topic = MqttTopics.DISP_1_SPEED,
            kind = TopicKind.CONTROL,
            description = "Скорость дозатора 1 (мл/мин)"
        ),

        TopicItem(
            id = "disp2_pow",
            title = "Дозатор 2 питание",
            topic = MqttTopics.DISP_2_POW,
            kind = TopicKind.CONTROL
        ),
        TopicItem(
            id = "disp2_speed",
            title = "Дозатор 2 скорость",
            topic = MqttTopics.DISP_2_SPEED,
            kind = TopicKind.CONTROL
        ),

        TopicItem(
            id = "react_temp_set",
            title = "Контроль температуры реактора",
            topic = MqttTopics.REACT_TEMP_SET,
            kind = TopicKind.CONTROL,
            description = "Включение/выключение контроля температуры"
        ),
        TopicItem(
            id = "react_temp_target",
            title = "Целевая температура реактора",
            topic = MqttTopics.REACT_TEMP_TARGET,
            kind = TopicKind.CONTROL,
            description = "Установка целевой температуры"
        ),
    )
}