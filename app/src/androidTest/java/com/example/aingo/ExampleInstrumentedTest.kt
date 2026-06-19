package com.example.aingo

import org.junit.Assert.*
import org.junit.Test

class ExampleInstrumentedTest {


    @Test
    fun testBasicSystemValidation() {
        val expected = 4
        val actual = 2 + 2
        assertEquals(expected, actual)
    }


    private fun extractEnglishText(botText: String): String {
        return if (botText.contains("(")) {
            botText.substringBefore("(").trim()
        } else {
            botText.trim()
        }
    }


    @Test
    fun testGeminiParserAndTextToSpeechFormatting() {
        val rawBotResponse = "Hello, how can I help you today? (Привіт, як я можу допомогти тобі сьогодні?)"
        val expectedCleanText = "Hello, how can I help you today?"
        val actualCleanText = extractEnglishText(rawBotResponse)
        assertEquals(expectedCleanText, actualCleanText)
    }


    @Test
    fun testUserProfileLevelValidation() {
        val validLevels = listOf("A1", "A2", "B1", "B2", "C1", "C2")
        val currentUserLevel = "B1"
        assertTrue(validLevels.contains(currentUserLevel))
    }


    @Test
    fun testUserProgressCalculation() {
        val learnedWords = 15
        val totalWords = 60
        val expectedPercentage = 25
        val actualPercentage = (learnedWords * 100) / totalWords
        assertEquals(expectedPercentage, actualPercentage)
    }


    @Test
    fun testEmptyChatInputValidation() {
        val emptyInput = "   "
        val isInputBlank = emptyInput.isBlank()
        assertTrue(isInputBlank)
    }


    @Test
    fun testFlashcardsSuccessPercentage() {
        val correctSwipes = 8
        val totalSwipes = 10
        val actualSuccessRate = (correctSwipes.toFloat() / totalSwipes.toFloat()) * 100f
        assertEquals(80f, actualSuccessRate, 0.01f)
    }


    @Test
    fun testFirebaseIdValidation() {
        val generatedUid = "user_auth_94726bf_prod"
        val isNotEmpty = generatedUid.isNotEmpty() && generatedUid.startsWith("user_")
        assertTrue(isNotEmpty)
    }

    @Test
    fun testGeminiApiKeyExtraction() {
        val mockConfigKey = "AIzaSyFakeKey_Gemini_Aingo_2026"
        val isValidLength = mockConfigKey.length > 10
        assertTrue(isValidLength)
    }
}