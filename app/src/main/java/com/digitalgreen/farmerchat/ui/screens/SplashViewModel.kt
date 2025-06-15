package com.digitalgreen.farmerchat.ui.screens

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.digitalgreen.farmerchat.FarmerChatApplication
import com.digitalgreen.farmerchat.utils.PreferencesManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SplashViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesManager = PreferencesManager(application)
    private val repository = (application as FarmerChatApplication).repository
    
    private val _hasCompletedOnboarding = MutableStateFlow<Boolean?>(null)
    val hasCompletedOnboarding: StateFlow<Boolean?> = _hasCompletedOnboarding
    
    init {
        viewModelScope.launch {
            try {
                Log.d("SplashViewModel", "=== SPLASH INIT START ===")
                
                // Sign in anonymously with Firebase
                val firebaseAuth = FirebaseAuth.getInstance()
                val currentUser = firebaseAuth.currentUser
                
                if (currentUser == null) {
                    Log.d("SplashViewModel", "No Firebase user, signing in anonymously")
                    firebaseAuth.signInAnonymously().await()
                    Log.d("SplashViewModel", "Firebase anonymous signin completed")
                } else {
                    Log.d("SplashViewModel", "Firebase user already exists: ${currentUser.uid}")
                }
                
                // Authenticate with our backend API (it gets the Firebase token internally)
                Log.d("SplashViewModel", "Starting backend authentication...")
                repository.authenticateWithFirebase().fold(
                    onSuccess = { authResponse ->
                        Log.d("SplashViewModel", "✅ Backend auth SUCCESS - token: ${authResponse.token.take(10)}...")
                        Log.d("SplashViewModel", "User ID: ${authResponse.user.id}")
                        Log.d("SplashViewModel", "Expires in: ${authResponse.expiresIn}s")
                    },
                    onFailure = { error ->
                        Log.e("SplashViewModel", "❌ Backend auth FAILED", error)
                    }
                )
                
                // Check onboarding status
                Log.d("SplashViewModel", "Checking onboarding status...")
                preferencesManager.hasCompletedOnboarding.collect { completed ->
                    Log.d("SplashViewModel", "Onboarding completed: $completed")
                    _hasCompletedOnboarding.value = completed
                }
            } catch (e: Exception) {
                Log.e("SplashViewModel", "Error during initialization", e)
                _hasCompletedOnboarding.value = false
            }
        }
    }
}