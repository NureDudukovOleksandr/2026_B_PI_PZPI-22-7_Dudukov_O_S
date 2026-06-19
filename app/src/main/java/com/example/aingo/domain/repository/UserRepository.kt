package com.example.aingo.domain.repository

import com.example.aingo.domain.model.ChatMessage
import com.example.aingo.domain.model.UserProfile
import com.example.aingo.domain.model.WordCard
import kotlinx.coroutines.flow.Flow


interface UserRepository {
    // Профіль
    suspend fun createOrUpdateUser()
    fun getUserProfile(): Flow<UserProfile?>
    suspend fun fetchUserProfile(): UserProfile?
    suspend fun updateWordsCount(newCount: Int)

    // Словник (для Flashcards)
    suspend fun saveWordCard(card: WordCard)
    suspend fun fetchVocabulary(): List<WordCard>
    suspend fun deleteWordCard(cardId: Int)
}