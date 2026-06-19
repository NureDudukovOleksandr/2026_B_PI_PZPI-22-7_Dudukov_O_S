package com.example.aingo.domain.model


data class ChatMessage(
    val text: String = "",
    @field:JvmField
    val isUser: Boolean = false,
    val timestamp: Long = 0
)