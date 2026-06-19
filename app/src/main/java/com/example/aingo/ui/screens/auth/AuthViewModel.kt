package com.example.aingo.ui.screens.auth

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aingo.data.remote.FirebaseAuthManager
import com.example.aingo.data.repository.UserRepositoryImpl
import com.example.aingo.ui.navigation.Screen
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authManager = FirebaseAuthManager(application)
    private val repository = UserRepositoryImpl()

    private val _currentUser = mutableStateOf(authManager.getCurrentUser())
    val currentUser: State<FirebaseUser?> = _currentUser

    var isSignInSuccessful = mutableStateOf(false)
        private set

    fun signInWithGoogle(credential: AuthCredential) {
        viewModelScope.launch {
            try {
                // Авторизація у Firebase
                val authResult = FirebaseAuth.getInstance().signInWithCredential(credential).await()

                if (authResult.user != null) {
                    repository.createOrUpdateUser()
                    _currentUser.value = authManager.getCurrentUser()
                    isSignInSuccessful.value = true
                }
            } catch (e: Exception) {
                android.util.Log.e("AINGO_AUTH", "Помилка: ${e.message}")
            }
        }
    }
}