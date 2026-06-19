package com.example.aingo.domain.model

import com.google.firebase.firestore.PropertyName


data class WordCard(
    val id: Int = 0,
    val original: String = "",
    val translation: String = "",
    val example: String = "",

    @get:PropertyName("isLearned")
    @set:PropertyName("isLearned")
    var isLearned: Boolean = false
)
