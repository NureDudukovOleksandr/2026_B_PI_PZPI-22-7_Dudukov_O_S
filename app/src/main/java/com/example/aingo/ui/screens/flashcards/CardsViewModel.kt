package com.example.aingo.ui.screens

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aingo.BuildConfig
import com.example.aingo.data.repository.UserRepositoryImpl
import com.example.aingo.domain.model.WordCard
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch

class FlashcardsViewModel : ViewModel() {

    private val userRepository = UserRepositoryImpl()

    private val _allCards = mutableStateListOf<WordCard>()

    var filterIndex by mutableStateOf(0) // 0 - New, 1 - Learned
    var isLoading by mutableStateOf(false)

    private val analyzerModel by lazy {
        GenerativeModel(modelName = "gemini-flash-latest", apiKey = BuildConfig.GEMINI_API_KEY)
    }

    // Геттер
    val wordCards: List<WordCard>
        get() = when (filterIndex) {
            0 -> _allCards.filter { !it.isLearned }
            1 -> _allCards.filter { it.isLearned }
            else -> emptyList()
        }

    init {
        loadCardsFromFirebase()
    }

    private fun loadCardsFromFirebase() {
        viewModelScope.launch {
            isLoading = true
            try {
                val cards = userRepository.fetchVocabulary()
                _allCards.clear()
                _allCards.addAll(cards)
                val learnedCount = cards.count { it.isLearned }
                Log.d("AINGO_DEBUG", "Loaded ${cards.size} cards. Learned: $learnedCount")
            } catch (e: Exception) {
                Log.e("AINGO_CARDS", "Load error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun onSyncClicked() = syncAllMessagesToCards()

    fun onCardSwiped(isKnown: Boolean, card: WordCard) {
        viewModelScope.launch {
            val index = _allCards.indexOfFirst { it.id == card.id }
            if (index == -1) return@launch

            if (filterIndex == 0) {
                if (isKnown) {
                    val updatedCard = card.copy(isLearned = true)
                    _allCards[index] = updatedCard
                    userRepository.saveWordCard(updatedCard)
                    userRepository.incrementUserField("wordsLearned")
                } else {
                    val item = _allCards.removeAt(index)
                    _allCards.add(0, item)
                }
            } else {
                //  ЛОГІКА LEARNED
                if (!isKnown) {
                    val updatedCard = card.copy(isLearned = false)
                    _allCards[index] = updatedCard
                    userRepository.saveWordCard(updatedCard)
                } else {
                    val item = _allCards.removeAt(index)
                    _allCards.add(0, item)
                }
            }
        }
    }

    private fun syncAllMessagesToCards() {
        viewModelScope.launch {
            if (isLoading) return@launch
            isLoading = true
            try {
                val allMessages = userRepository.fetchChatHistory()
                val teacherText = allMessages
                    .filter { !it.isUser }
                    .takeLast(15)
                    .joinToString("\n") { it.text }

                if (teacherText.isBlank()) {
                    isLoading = false
                    return@launch
                }

                val prompt = """
                    Extract 3-5 English words or phrases: "$teacherText".
                    Format: word|translation|example. Ukrainian translation. No markdown.
                """.trimIndent()

                val response = analyzerModel.generateContent(prompt)
                val result = response.text ?: ""

                result.lines().forEach { line ->
                    if (line.contains("|")) {
                        val parts = line.split("|")
                        if (parts.size >= 3) {
                            val word = parts[0].trim()
                            if (_allCards.none { it.original.equals(word, ignoreCase = true) }) {
                                val newCard = WordCard(
                                    id = (System.currentTimeMillis() % 1000000).toInt() + (0..100).random(),
                                    original = word,
                                    translation = parts[1].trim(),
                                    example = parts[2].trim(),
                                    isLearned = false
                                )
                                userRepository.saveWordCard(newCard)
                                _allCards.add(newCard)
                            }
                        }
                    }
                }
            } finally {
                isLoading = false
            }
        }
    }
}