package com.example.aingo.data.local

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

object AppDatabase {

    private val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    val usersCollection: CollectionReference
        get() = db.collection("users")

    fun getMessagesCollection(uid: String): CollectionReference {
        return usersCollection.document(uid).collection("messages")
    }

    fun getVocabularyCollection(uid: String): CollectionReference {
        return usersCollection.document(uid).collection("vocabulary")
    }

}