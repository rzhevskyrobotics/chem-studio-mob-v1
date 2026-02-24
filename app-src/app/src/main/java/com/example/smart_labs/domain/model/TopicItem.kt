package com.example.smart_labs.domain.model

enum class TopicKind {
    SENSOR,   // отображаемое значение
    CONTROL   // управление (toggle/slider)
}

data class TopicItem(
    val id: String,
    val title: String,
    val topic: String,
    val kind: TopicKind,
    val description: String? = null
)