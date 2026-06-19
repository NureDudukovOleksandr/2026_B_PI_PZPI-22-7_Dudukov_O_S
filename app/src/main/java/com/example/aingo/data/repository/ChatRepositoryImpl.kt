package com.example.aingo.data.repository

import android.util.Log
import com.example.aingo.data.local.AppDatabase
import com.example.aingo.domain.model.ChatMessage
import com.example.aingo.domain.repository.ChatRepository
import com.example.aingo.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await



class ChatRepositoryImpl(
    private val userRepository: UserRepository
) : ChatRepository {

    private val auth = FirebaseAuth.getInstance()
    private val tag = "AINGO_CHAT_REPO"

    override suspend fun saveMessage(message: ChatMessage) {
        val uid = auth.currentUser?.uid ?: return
        try {

            AppDatabase.getMessagesCollection(uid).add(message).await()


            Log.d(tag, "Message saved to Firestore")
        } catch (e: Exception) {
            Log.e(tag, "Error saving message: ${e.message}")
        }
    }

    override fun getMessages(): Flow<List<ChatMessage>> = callbackFlow {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            trySend(emptyList())
            close()
        } else {
            val query = AppDatabase.getMessagesCollection(uid)
                .orderBy("timestamp", Query.Direction.ASCENDING)

            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(tag, "Listen failed: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val messages = snapshot.toObjects(ChatMessage::class.java)
                    trySend(messages)
                }
            }

            awaitClose { listener.remove() }
        }
    }
}