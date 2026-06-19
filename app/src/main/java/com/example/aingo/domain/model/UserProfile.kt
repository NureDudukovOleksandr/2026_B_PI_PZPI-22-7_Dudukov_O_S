package com.example.aingo.domain.model



data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val level: String = "A1",
    val wordsLearned: Int = 0,
    val totalChats: Int = 0,
    val lastActive: Long = System.currentTimeMillis()
) {
    constructor(map: Map<String, Any?>) : this(
        uid = map["uid"] as? String ?: "",
        name = map["name"] as? String ?: "",
        email = map["email"] as? String ?: "",
        level = map["level"] as? String ?: "A1",
        wordsLearned = (map["wordsLearned"] as? Long)?.toInt() ?: 0,
        totalChats = (map["totalChats"] as? Long)?.toInt() ?: 0,
        lastActive = map["lastActive"] as? Long ?: System.currentTimeMillis()
    )

    fun toMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "name" to name,
        "email" to email,
        "level" to level,
        "wordsLearned" to wordsLearned,
        "totalChats" to totalChats,
        "lastActive" to lastActive
    )
}