package com.example.aingo.domain.repository

import com.example.aingo.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun saveMessage(message: ChatMessage)
    fun getMessages(): Flow<List<ChatMessage>>
}