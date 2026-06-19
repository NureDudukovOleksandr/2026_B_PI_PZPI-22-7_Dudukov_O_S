package com.example.aingo

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import com.example.aingo.data.remote.FirebaseAuthManager
import com.example.aingo.ui.navigation.AppNavigation
import com.example.aingo.ui.screens.auth.AuthScreen
import com.example.aingo.ui.screens.auth.AuthViewModel
import com.example.aingo.ui.theme.AingoTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : ComponentActivity() {
    private val authManager by lazy { FirebaseAuthManager(this) }
    private val authViewModel: AuthViewModel by viewModels()

    // Лаунчер для вікна вибору Google
    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            authViewModel.signInWithGoogle(credential)

        } catch (e: ApiException) {
            Log.e("AUTH_DEBUG", "Google Sign-In failed: ${e.statusCode}")
            Toast.makeText(this, "Помилка авторизації: ${e.statusCode}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AingoTheme {
                val user by authViewModel.currentUser
                if (user == null) {
                    AuthScreen(onSignInClick = {
                        val intent = authManager.getSignInIntent()
                        signInLauncher.launch(intent)
                    })
                } else {
                    AppNavigation()
                }
            }
        }
    }
}