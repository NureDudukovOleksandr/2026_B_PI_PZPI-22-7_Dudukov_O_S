package com.example.aingo.ui.chat

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.RecognizerIntent.EXTRA_LANGUAGE
import android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aingo.BuildConfig
import com.example.aingo.data.repository.ChatRepositoryImpl
import com.example.aingo.data.repository.UserRepositoryImpl
import com.example.aingo.domain.model.ChatMessage
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class ChatViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(application, this)
    private val userRepository = UserRepositoryImpl()
    private val chatRepository = ChatRepositoryImpl(userRepository)
    private val speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(application)

    val messages: StateFlow<List<ChatMessage>> = chatRepository.getMessages()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    var isRecording by mutableStateOf(false)
        private set

    var isSoundEnabled by mutableStateOf(true)

    private var generativeModel by mutableStateOf<GenerativeModel?>(null)

    init {
        viewModelScope.launch {
            userRepository.getUserProfile().collect { profile ->
                profile?.let {
                    val name = it.name.ifBlank { "Student" }
                    val level = it.level.ifBlank { "A1" }

                    generativeModel = GenerativeModel(
                        modelName = "gemini-flash-latest",
                        apiKey = BuildConfig.GEMINI_API_KEY,
                        systemInstruction = content {
                            text("""
                                You are Aingo, a professional English teacher. 
                                Student's name: $name. 
                                Student's level: $level.
                                
                                Rules:
                                1. Respond only in English.
                                2. Be encouraging and use vocabulary appropriate for level $level.
                                3. At the very end of your message, add a full Ukrainian translation in parentheses like this: (Ваш переклад тут).
                            """.trimIndent())
                        }
                    )
                }
            }
        }
    }

    fun startListening(onResult: (String) -> Unit) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString())
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { isRecording = true }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { isRecording = false }
            override fun onError(error: Int) { isRecording = false }
            override fun onResults(results: Bundle?) {
                val data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                data?.get(0)?.let { onResult(it) }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        speechRecognizer.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer.stopListening()
        isRecording = false
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return
        val currentModel = generativeModel ?: return

        viewModelScope.launch {
            try {
                chatRepository.saveMessage(ChatMessage(userText, true, System.currentTimeMillis()))
                userRepository.incrementUserField("totalChats")

                val response = currentModel.generateContent(userText)
                response.text?.let { botText ->
                    chatRepository.saveMessage(ChatMessage(botText, false, System.currentTimeMillis()))
                    if (isSoundEnabled) speak(extractEnglishText(botText))
                }
            } catch (e: Exception) {
                Log.e("AINGO_CHAT", "Error: ${e.message}")
            }
        }
    }

    private fun extractEnglishText(text: String): String = text.split("(").first().trim()
    fun toggleSound() { isSoundEnabled = !isSoundEnabled }
    private fun speak(text: String) { tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null) }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            tts?.setSpeechRate(0.85f)
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.shutdown()
        speechRecognizer.destroy()
    }
}
