package com.example.aingo.ui.screens

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aingo.BuildConfig
import com.example.aingo.data.repository.UserRepositoryImpl
import com.example.aingo.domain.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.example.aingo.data.local.AppDatabase
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val repository = UserRepositoryImpl()

    var userProfile by mutableStateOf<UserProfile?>(null)
        private set
    var learnedWordsCount by mutableIntStateOf(0)
        private set

    var aiDetectedLevel by mutableStateOf("")
    var aiFeedback by mutableStateOf("")
    var isAnalyzingLevel by mutableStateOf(false)

    private val analyzerModel by lazy {
        GenerativeModel(modelName = "gemini-flash-latest", apiKey = BuildConfig.GEMINI_API_KEY)
    }

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            launch {
                repository.getUserProfile().collect { profile ->
                    userProfile = profile
                }
            }
            updateLearnedCount()
        }
    }

    fun updateLearnedCount() {
        viewModelScope.launch {
            try {
                val vocabulary = repository.fetchVocabulary()
                learnedWordsCount = vocabulary.count { it.isLearned }
            } catch (e: Exception) {
                learnedWordsCount = 0
            }
        }
    }

    fun assessEnglishLevel() {
        viewModelScope.launch {
            val vocabulary = repository.fetchVocabulary().filter { it.isLearned }
            val chatHistory = repository.fetchChatHistory().filter { it.isUser }

            if (vocabulary.size < 5 || chatHistory.size < 5) {
                aiFeedback = "Not enough data yet. Chat more and learn at least 5 cards to get an AI assessment!"
                aiDetectedLevel = ""
                return@launch
            }

            isAnalyzingLevel = true
            try {
                val wordsText = vocabulary.joinToString(", ") { it.original }
                val messagesText = chatHistory.takeLast(15).joinToString("\n") { it.text }

                val prompt = """
                    Analyze English level based on:
                    Words: $wordsText
                    Messages: $messagesText
                    Provide result strictly in format: Level | Encouraging advice (1 sentence).
                    Levels: A1, A2, B1, B2, C1, C2.
                """.trimIndent()

                val response = analyzerModel.generateContent(prompt)
                val result = response.text ?: ""

                if (result.contains("|")) {
                    val parts = result.split("|")
                    aiDetectedLevel = parts[0].trim()
                    aiFeedback = parts[1].trim()
                }
            } catch (e: Exception) {
                Log.e("PROFILE_AI", "Error: ${e.message}")
                aiFeedback = "Assessment failed. Try again later."
            } finally {
                isAnalyzingLevel = false
            }
        }
    }

    fun updateProfile(newName: String, newLevel: String) {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            AppDatabase.usersCollection.document(uid)
                .update(mapOf(
                    "name" to newName,
                    "level" to newLevel
                ))
        }
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        FirebaseAuth.getInstance().signOut()
        onLogoutSuccess()
    }
}