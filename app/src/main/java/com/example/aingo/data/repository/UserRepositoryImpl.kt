package com.example.aingo.data.repository


import android.util.Log
import com.example.aingo.data.local.AppDatabase
import com.example.aingo.domain.model.ChatMessage
import com.example.aingo.domain.model.UserProfile
import com.example.aingo.domain.model.WordCard
import com.example.aingo.domain.repository.UserRepository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore // Додано імпорт
import com.google.firebase.firestore.Transaction // Додано імпорт
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await


class UserRepositoryImpl : UserRepository {
    private val auth = FirebaseAuth.getInstance()

    private val firestore = FirebaseFirestore.getInstance()
    private val tag = "AINGO_USER_REPO"

    override suspend fun createOrUpdateUser() {
        val user = auth.currentUser ?: return
        val userRef = AppDatabase.usersCollection.document(user.uid)
        try {
            val snapshot = userRef.get().await()
            if (!snapshot.exists()) {
                val newProfile = UserProfile(
                    uid = user.uid,
                    name = user.displayName ?: "Learner",
                    email = user.email ?: "",
                    level = "A1"
                )
                userRef.set(newProfile.toMap()).await()
            }
        } catch (e: Exception) {
            Log.e(tag, "Sync error: ${e.message}")
        }
    }

    override fun getUserProfile(): Flow<UserProfile?> = callbackFlow {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            trySend(null)
            close()
        } else {
            val listener = AppDatabase.usersCollection.document(uid)
                .addSnapshotListener { snapshot, _ ->
                    snapshot?.data?.let { trySend(UserProfile(it)) }
                }
            awaitClose { listener.remove() }
        }
    }

    override suspend fun fetchUserProfile(): UserProfile? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val snapshot = AppDatabase.usersCollection.document(uid).get().await()
            snapshot.data?.let { UserProfile(it) }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateWordsCount(newCount: Int) {
        val uid = auth.currentUser?.uid ?: return
        try {
            AppDatabase.usersCollection.document(uid).update("wordsLearned", newCount).await()
        } catch (e: Exception) {
            Log.e(tag, "Update count error: ${e.message}")
        }
    }


    override suspend fun saveWordCard(card: WordCard) {
        val uid = auth.currentUser?.uid ?: return
        val cardRef = AppDatabase.getVocabularyCollection(uid).document(card.id.toString())


        val data = hashMapOf(
            "id" to card.id,
            "original" to card.original,
            "translation" to card.translation,
            "example" to card.example,
            "isLearned" to card.isLearned
        )
        cardRef.set(data).await()
    }

    override suspend fun fetchVocabulary(): List<WordCard> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = AppDatabase.getVocabularyCollection(uid).get().await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    WordCard(
                        id = doc.getLong("id")?.toInt() ?: 0,
                        original = doc.getString("original") ?: "",
                        translation = doc.getString("translation") ?: "",
                        example = doc.getString("example") ?: "",
                        isLearned = doc.getBoolean("isLearned") ?: false
                    )
                } catch (e: Exception) {
                    Log.e(tag, "Mapping error for doc ${doc.id}: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Fetch vocabulary error: ${e.message}")
            emptyList()
        }
    }

    override suspend fun deleteWordCard(cardId: Int) {
        val uid = auth.currentUser?.uid ?: return
        try {
            AppDatabase.getVocabularyCollection(uid).document(cardId.toString()).delete().await()
        } catch (e: Exception) {
            Log.e(tag, "Delete card error: ${e.message}")
        }
    }


    suspend fun incrementUserField(fieldName: String) {
        val uid = auth.currentUser?.uid ?: return
        try {
            firestore.collection("users")
                .document(uid)
                .update(fieldName, FieldValue.increment(1))
                .await()
        } catch (e: Exception) {
            android.util.Log.e("REPO", "Increment failed: ${e.message}")
        }
    }

     suspend fun fetchChatHistory(): List<ChatMessage> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = AppDatabase.getMessagesCollection(uid)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(20) // Беремо останні 20 повідомлень
                .get()
                .await()

            val list = snapshot.toObjects(ChatMessage::class.java)
            Log.d("AINGO_DEBUG", "Знайдено повідомлень у базі: ${list.size}")
            list
        } catch (e: Exception) {
            Log.e("AINGO_DEBUG", "Помилка завантаження історії: ${e.message}")
            emptyList()
        }
    }
}